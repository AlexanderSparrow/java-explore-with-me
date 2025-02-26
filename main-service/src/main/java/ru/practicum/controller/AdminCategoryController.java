package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto dto) {
        log.info("Получен запрос на добавление категории: {}", dto);
        return categoryService.addCategory(dto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Получен запрос на удаление категории: {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid CategoryDto dto) {
        log.info("Получен запрос на изменение категории: {}", dto);
        return categoryService.updateCategory(catId, dto);
    }
}
