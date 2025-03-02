package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.*;
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
                                               Boolean onlyAvailable, String sort, int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size, getSort(sort));

        List<Event> events = eventRepository.findPublishedEvents(
                text != null ? text.toLowerCase() : null,
                categories, paid, rangeStart != null ? rangeStart : LocalDateTime.now(), rangeEnd, pageable);

        return events.stream()
                .filter(event -> !onlyAvailable || requestRepository.countByEventAndStatus(event.getId(), RequestStatus.CONFIRMED) < event.getParticipantLimit())
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getPublicEventById(Long eventId) {
        Event event = eventRepository.findPublishedEventById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));
        return eventMapper.toEventFullDto(event);
    }

    private Sort getSort(String sort) {
        return "VIEWS".equalsIgnoreCase(sort) ? Sort.by(Sort.Direction.DESC, "views") : Sort.by(Sort.Direction.ASC, "eventDate");
    }


    public void trackEventView(Long eventId, String ip) {
        EndpointHitDto hit = new EndpointHitDto("ExploreWithMe", "/events/" + eventId, ip, LocalDateTime.now());
        log.info("Отправка статистики: {}", hit);
        statsClient.sendHit(hit);
    }
}
