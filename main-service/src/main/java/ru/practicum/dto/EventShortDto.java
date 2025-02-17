package ru.practicum.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private LocalDateTime eventDate;
    private Integer confirmedRequests;
    private Integer views;
}