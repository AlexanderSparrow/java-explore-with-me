package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.exception.AppException;
import ru.practicum.service.PublicEventService;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventService eventService;
    private final StatsService statsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<EventShortDto>> getEvents(
            @RequestParam(required = false, defaultValue = "") String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false, value = "paid") Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            HttpServletRequest request) {

        log.info("Запрос событий: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new AppException("Ошибка: начальная дата не может быть позже конечной.", HttpStatus.BAD_REQUEST);
        }

        List<EventShortDto> events = eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (!sort.equals("EVENT_DATE") && !sort.equals("VIEWS")) {
            throw new AppException("Ошибка: сортировка должна быть EVENT_DATE или VIEWS.", HttpStatus.BAD_REQUEST);
        }

        // Логируем в сервис статистики
        statsService.saveHit((EndpointHitDto) request);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Запрос события с идентификатором {}", id);

        EventFullDto event = eventService.getPublicEventById(id);

        // Логируем в сервис статистики
        statsService.saveHit((EndpointHitDto) request);

        return ResponseEntity.ok(event);
    }
}
