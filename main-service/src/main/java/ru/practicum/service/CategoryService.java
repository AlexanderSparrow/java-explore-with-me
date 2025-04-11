package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Transactional
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "id"));
        List<CategoryDto> categories = categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
        log.info("Найдены категории: {} ", categories);
        return categories;
    }

    @Transactional
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new AppException("Категория id=" + catId + " не найдена.", HttpStatus.NOT_FOUND));
        log.info("Найдена категория {}:", category);
        return categoryMapper.toDto(category);
    }

    @Transactional
    public CategoryDto addCategory(NewCategoryDto dto) {
        Category category = categoryMapper.toCategory(dto);
        log.info("Сохраняем категорию: {}", category);
        try {
            return categoryMapper.toDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            log.warn("Ошибка при сохранении категории — дубликат имени: {}", dto.getName());
            throw new AppException("Название категории должно быть уникальным.", HttpStatus.CONFLICT);
        }
    }


    @Transactional
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new AppException("Категория id=" + catId + " не найдена.", HttpStatus.NOT_FOUND);
        }

        if (eventRepository.existsByCategory_Id(catId)) {
            throw new AppException("Нельзя удалить категорию с привязанными событиями", HttpStatus.CONFLICT);
        }

        log.info("Удаляем категорию: {}", catId);
        categoryRepository.deleteById(catId);
    }

    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new AppException("Категория id=" + catId + " не найдена.", HttpStatus.NOT_FOUND));
        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new AppException("Название категории должно быть уникальным.", HttpStatus.CONFLICT);
        }
        category.setName(dto.getName());
        log.info("Категория изменена на: {}", category);
        return categoryMapper.toDto(categoryRepository.save(category));
    }
}
