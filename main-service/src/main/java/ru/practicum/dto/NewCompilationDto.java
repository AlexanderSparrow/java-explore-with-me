package ru.practicum.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class NewCompilationDto {
    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned = false;

    private Set<Long> events;
}
