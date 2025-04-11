package ru.practicum.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class ViewsStatsRequest {

    @Singular("uri")
    private Set<String> uris;

    @Builder.Default
    private LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0);

    @Builder.Default
    private LocalDateTime end = LocalDateTime.now();

    private boolean unique;
}
