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
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.ViewsStatsRequest;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateAction;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEventService {

    private static final Map<StateAction, EventState> statusMap = Map.of(
            StateAction.PUBLISH_EVENT, EventState.PUBLISHED,
            StateAction.REJECT_EVENT, EventState.CANCELED
    );

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;

    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        if (from < 0 || size <= 0) {
            throw new AppException("Некорректные параметры пагинации.", HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());

        boolean filterUsers = users != null && !users.isEmpty() && !(users.size() == 1 && users.getFirst() == 0);
        boolean filterStates = states != null && !states.isEmpty();
        boolean filterCategories = categories != null && !categories.isEmpty() && !(categories.size() == 1 && categories.getFirst() == 0);
        boolean filterDates = rangeStart != null && rangeEnd != null && rangeStart.isBefore(rangeEnd);

        List<Event> events = eventRepository.findWithFilters(
                filterUsers ? users : null,
                filterStates ? states : null,
                filterCategories ? categories : null,
                filterDates ? rangeStart : null,
                filterDates ? rangeEnd : null,
                null,
                null,
                pageable
        );

        if (events.isEmpty()) return List.of();

        Map<Long, Long> confirmedRequestsMap = getconfirmedRequestsMap(events);

        Map<String, Event> uriToEventMap = events.stream()
                .filter(e -> e.getPublishedOn() != null)
                .collect(Collectors.toMap(e -> "/events/" + e.getId(), e -> e));

        List<ViewStats> stats = statsClient.getStats(
                uriToEventMap.entrySet().stream()
                        .map(entry -> ViewsStatsRequest.builder()
                                .uri(entry.getKey())
                                .start(entry.getValue().getPublishedOn())
                                .end(LocalDateTime.now())
                                .unique(true)
                                .build())
                        .toList()
        );

        Map<String, Long> viewsMap = stats.stream()
                .collect(Collectors.toMap(ViewStats::getUri, ViewStats::getHits));

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event);
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                }).collect(Collectors.toList());
    }

    private Map<Long, Long> getconfirmedRequestsMap(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> map = participationRequestRepository
                .countConfirmedRequestsForEvents(eventIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        return map;
    }

    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new AppException("Дата начала изменяемого события должна быть не ранее чем за час от текущего времени.",
                    HttpStatus.BAD_REQUEST);
        }

        if (event.getState() == EventState.PUBLISHED && updateRequest.getStateAction() == StateAction.PUBLISH_EVENT) {
            throw new AppException("Нельзя публиковать уже опубликованное событие.", HttpStatus.CONFLICT);
        }

        if (event.getState() == EventState.CANCELED && updateRequest.getStateAction() == StateAction.PUBLISH_EVENT) {
            throw new AppException("Нельзя опубликовать отклонённое событие.", HttpStatus.CONFLICT);
        }

        if (event.getState() == EventState.PUBLISHED && updateRequest.getStateAction() == StateAction.REJECT_EVENT) {
            throw new AppException("Нельзя отклонить уже опубликованное событие.", HttpStatus.CONFLICT);
        }

        updateField(updateRequest.getTitle(), event::setTitle);
        updateField(updateRequest.getAnnotation(), event::setAnnotation);
        updateField(updateRequest.getDescription(), event::setDescription);

        if (updateRequest.getCategory() != null) {
            var category = categoryRepository.getCategoryById(updateRequest.getCategory());
            if (category == null) {
                throw new AppException("Категория с id=" + updateRequest.getCategory() + " не найдена.", HttpStatus.NOT_FOUND);
            }
            event.setCategory(category);
        }

        updateField(updateRequest.getEventDate(), event::setEventDate);
        updateField(updateRequest.getLocation(), event::setLocation);
        updateField(updateRequest.getPaid(), event::setPaid);
        updateField(updateRequest.getParticipantLimit(), event::setParticipantLimit);

        EventState newState = Optional.ofNullable(updateRequest.getStateAction())
                .map(statusMap::get)
                .orElse(event.getState());

        event.setState(newState);

        if (newState == EventState.PUBLISHED) {
            event.setPublishedOn(LocalDateTime.now());
        }

        Event saved = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(saved);
        dto.setConfirmedRequests(participationRequestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED));

        if (event.getPublishedOn() != null) {
            ViewsStatsRequest statsRequest = ViewsStatsRequest.builder()
                    .uri("/events/" + eventId)
                    .start(event.getPublishedOn())
                    .end(LocalDateTime.now())
                    .unique(true)
                    .build();

            List<ViewStats> stats = statsClient.getStats(List.of(statsRequest));
            dto.setViews(stats.isEmpty() ? 0L : stats.getFirst().getHits());
        } else {
            dto.setViews(0L);
        }

        return dto;
    }

    private <T> void updateField(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }
}
