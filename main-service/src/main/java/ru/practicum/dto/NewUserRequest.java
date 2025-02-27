package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest {

    @Email
    @NotBlank
    @NotNull
    @Size(min = 6, max = 254)
    private String email;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 250)
    private String name;
}
