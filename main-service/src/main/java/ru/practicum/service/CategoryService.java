package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория id=" + catId + " не найдена."));
        return categoryMapper.toDto(category);
    }

    @Transactional
    public CategoryDto addCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Название категории должно быть уникальным.");
        }
        Category category = categoryMapper.toCategory(dto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория id=" + catId + " не найдена.");
        }
        categoryRepository.deleteById(catId);
    }

    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория id=" + catId + " не найдена."));
        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Category name must be unique");
        }
        category.setName(dto.getName());
        return categoryMapper.toDto(categoryRepository.save(category));
    }
}
