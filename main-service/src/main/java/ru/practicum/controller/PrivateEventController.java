package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.service.PrivateEventService;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final PrivateEventService privateEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId, @RequestBody NewEventDto newEventDto) {
        log.info("Получен запрос на создание события: {}", newEventDto);
        return privateEventService.createEvent(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventFullDto eventFullDto) {
        log.info("Получен запрос на изменения события: {}", eventFullDto);
        return privateEventService.updateEvent(userId, eventId, eventFullDto);
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("Получен запрос на получение событий, созданных пользователем id: {}", userId);
        return privateEventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получен запрос на просмотр полной информации о событии id: {}", eventId);
        return privateEventService.getUserEventById(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получен запрос на участии в событии id: {}", eventId);
        return privateEventService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Получен запрос на обновление статуса запроса: {}", request);
        return privateEventService.updateRequestStatus(userId, eventId, request);
    }
}

