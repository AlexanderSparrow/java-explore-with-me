package ru.practicum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

/**
 * Контроллер для управления категориями (административные операции).
 */
@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    /**
     * Добавление новой категории.
     *
     * @param dto Данные добавляемой категории.
     * @return Сохраненная категория.
     */
    @Operation(summary = "Добавление категории", description = "Создает новую категорию и возвращает её данные.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Категория успешно создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "409", description = "Категория с таким именем уже существует")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto dto) {
        log.info("Получен запрос на добавление категории: {}", dto);
        return categoryService.addCategory(dto);
    }

    /**
     * Удаление категории по ID.
     *
     * @param catId ID категории для удаления.
     */
    @Operation(summary = "Удаление категории", description = "Удаляет категорию по её ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Категория успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена"),
            @ApiResponse(responseCode = "409", description = "Нельзя удалить категорию, если к ней привязаны события")
    })
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Parameter(description = "ID категории") Long catId) {
        log.info("Получен запрос на удаление категории: {}", catId);
        categoryService.deleteCategory(catId);
    }

    /**
     * Обновление данных категории.
     *
     * @param catId ID изменяемой категории.
     * @param dto   Обновленные данные категории.
     * @return Обновленный объект категории.
     */
    @Operation(summary = "Обновление категории", description = "Обновляет данные существующей категории.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(
            @PathVariable @Parameter(description = "ID категории") Long catId,
            @RequestBody @Valid CategoryDto dto) {
        log.info("Получен запрос на изменение категории: {}", dto);
        return categoryService.updateCategory(catId, dto);
    }
}
