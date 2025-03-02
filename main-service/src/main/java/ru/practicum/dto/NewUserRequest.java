package ru.practicum.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NewUserRequest {

    @Email
    @NotBlank
    @NotNull
    @Pattern(regexp = "^(?!\\s*$).+", message = "Email не может состоять только из пробелов")

    @Size(min = 6, max = 254)
    private String email;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 250)
    @Pattern(regexp = "^(?!\\s*$).+", message = "Имя не может состоять только из пробелов")
    private String name;
}
