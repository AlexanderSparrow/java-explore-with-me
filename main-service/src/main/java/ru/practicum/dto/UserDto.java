package ru.practicum.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class UserDto {
    private Long id;

    @NotBlank
    @Pattern(regexp = "^(?!\\s*$).+", message = "Email не может состоять только из пробелов")
    private String name;

    @NotBlank
    @Email
    @Pattern(regexp = "^(?!\\s*$).+", message = "Email не может состоять только из пробелов")
    private String email;
}

