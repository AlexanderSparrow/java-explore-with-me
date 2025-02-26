package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.enums.EventState;
import ru.practicum.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final PublicEventService eventService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        List<EventFullDto> events = eventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(
            @PathVariable Long eventId,
            @RequestBody UpdateEventAdminRequest updateRequest) {

        EventFullDto updatedEvent = eventService.updateEventAdmin(eventId, updateRequest);
        return ResponseEntity.ok(updatedEvent);
    }
}
