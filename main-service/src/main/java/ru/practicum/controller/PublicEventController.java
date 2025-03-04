package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.enums.EventSort;
import ru.practicum.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<EventShortDto>> getEvents(
            @RequestParam(required = false, defaultValue = "") String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        log.info("Запрос публичных событий: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        List<EventShortDto> events = eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        statsClient.sendHit(new EndpointHitDto("ExploreWithMe", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now()));

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Запрос события с идентификатором {}", id);

        EventFullDto event = eventService.getPublicEventById(id);

        statsClient.sendHit(new EndpointHitDto("ExploreWithMe", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now()));

        return ResponseEntity.ok(event);
    }
}
