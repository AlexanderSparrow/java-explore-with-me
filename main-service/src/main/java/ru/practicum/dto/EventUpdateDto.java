package ru.practicum.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventUpdateDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private Long category;
    private LocalDateTime eventDate;
    private Boolean paid;
    private Integer participantLimit;
}
