package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventForCommentsDto {

    @NotNull
    private CategoryDto category;

    @NotBlank
    private String title;
}
