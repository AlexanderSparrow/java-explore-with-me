package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@NotBlank(message = "Название категории должно быть указано.")
public class
NewCategoryDto {
    @Size(min = 1, max = 50)
    private String name;
}

