package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserDto {
    private Long id;
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}

