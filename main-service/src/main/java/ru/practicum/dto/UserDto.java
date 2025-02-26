package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}

