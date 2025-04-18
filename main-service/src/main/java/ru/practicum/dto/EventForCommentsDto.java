package ru.practicum.dto;

import lombok.Data;

@Data
public class EventForCommentsDto {
    private CategoryDto category;
    private String title;
}
