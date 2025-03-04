package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicEventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, EventSort sort, int from, int size) {

        if (from < 0 || size <= 0) {
            throw new AppException("Ошибка: некорректные параметры пагинации", HttpStatus.BAD_REQUEST);
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new AppException("Ошибка: начальная дата не может быть позже конечной.", HttpStatus.BAD_REQUEST);
        }

        // Если диапазон дат не задан, ищем события, которые произойдут позже текущего момента
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        Pageable pageable = PageRequest.of(from / size, size, getSort(sort));

        List<Event> events = eventRepository.findPublishedEvents(
                text != null && !text.isBlank() ? text.toLowerCase() : null,
                categories, paid, rangeStart, rangeEnd, pageable);

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        // Получение просмотров из статистики
        String start = rangeStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = rangeEnd != null ? rangeEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "9999-12-31 23:59:59";
        List<ViewStats> stats = statsClient.getStats(start, end);

        return events.stream()
                .filter(event -> !onlyAvailable || requestRepository.countByEventAndStatus(event.getId(), RequestStatus.CONFIRMED) < event.getParticipantLimit())
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);

                    // Подсчет просмотров
                    dto.setViews(stats.stream()
                            .filter(stat -> stat.getUri().endsWith("/events/" + event.getId()))
                            .mapToLong(ViewStats::getHits)
                            .sum());

                    // Подсчет одобренных заявок
                    dto.setConfirmedRequests(requestRepository.countByEventAndStatus(event.getId(), RequestStatus.CONFIRMED));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public EventFullDto getPublicEventById(Long eventId) {
        Event event = eventRepository.findPublishedEventById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));
        return eventMapper.toEventFullDto(event);
    }

    private Sort getSort(EventSort sort) {
        return sort == EventSort.VIEWS
                ? Sort.by(Sort.Direction.DESC, "views") :
                Sort.by(Sort.Direction.ASC, "eventDate");
    }
}
