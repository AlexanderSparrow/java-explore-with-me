package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserShortDto {
    @NotNull
    private Long id;

    @NotBlank
    private String name;
}

