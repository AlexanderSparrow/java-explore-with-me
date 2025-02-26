package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.service.PrivateService;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final PrivateService privateService;

    @PostMapping
    public EventFullDto createEvent(@PathVariable Long userId, @RequestBody NewEventDto newEventDto) {
        log.info("Create new event: {}", newEventDto);
        return privateService.createEvent(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventFullDto eventFullDto) {
        log.info("Update event: {}", eventFullDto);
        return privateService.updateEvent(userId, eventId, eventFullDto);
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("Get user events: {}", userId);
        return privateService.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Get user event: {}", eventId);
        return privateService.getUserEventById(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Get event requests: {}", eventId);
        return privateService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Update event request: {}", request);
        return privateService.updateRequestStatus(userId, eventId, request);
    }
}

