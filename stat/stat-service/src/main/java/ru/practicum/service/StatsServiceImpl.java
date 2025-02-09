package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public void saveHit(EndpointHit hit) {
        statsRepository.save(hit);
        log.info("Сохранили запрос {}", hit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Получение статистики с параметрами: start = {}, end = {}, uris = {}, unique = {}", start, end, uris, unique);
        List<ViewStats> result = statsRepository.getStats(start, end, uris, unique);
        log.info("Результат: {}", result);
        return result;
    }
}
