package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCategoryDto {
    @Size(min = 1, max = 50)
    @NotBlank(message = "Название категории должно быть указано.")
    private String name;
}

