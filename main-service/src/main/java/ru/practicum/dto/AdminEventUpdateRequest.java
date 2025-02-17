package ru.practicum.dto;

import lombok.Data;

@Data
public class AdminEventUpdateRequest {
    private String title;
    private String annotation;
    private String description;
    private Long category;
    private Boolean paid;
    private Integer participantLimit;
}
