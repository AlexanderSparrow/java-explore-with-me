package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final EndpointHitMapper mapper;

    @Override
    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit hit = mapper.toEntity(hitDto);
        statsRepository.save(hit);
        log.info("Сохранили запрос {}", hit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Получение статистики с параметрами: start = {}, end = {}, uris = {}, unique = {}", start, end, uris, unique);
        if (end.isBefore(start)) {
            throw new ValidationException("Дата начала периода не может быть ранее даты окончания периода.");
        }

        List<ViewStats> result = statsRepository.getStats(start, end, uris, unique);
        log.info("Результат: {}", result);
        return result;
    }
}
