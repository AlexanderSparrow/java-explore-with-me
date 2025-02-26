package ru.practicum.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Получение списка категорий с параметрами: from={}, size={}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("Получение категории по id={}", catId);
        return categoryService.getCategoryById(catId);
    }
}
