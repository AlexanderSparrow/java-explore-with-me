package ru.practicum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ViewsStatsRequest {
    private String uri;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean unique;
}
