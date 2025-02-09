package ru.practicum.service;

import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(EndpointHit hit);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
