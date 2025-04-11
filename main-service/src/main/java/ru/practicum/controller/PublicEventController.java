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

/**
 * Контроллер для управления событиями (публичная часть).
 */

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventService eventService;
    private final StatsClient statsClient;

    /**
     * Добавление получения списка событий с возможностью фильтрации.
     *
     * @param text          - данные для поиска по полям события.
     * @param categories    - список идентификаторов категорий.
     * @param paid          - отбор по критерию "платное" мероприятие.
     * @param rangeStart    - начало периода для поиска событий.
     * @param rangeEnd      - конец периода для поиска событий.
     * @param from          - пропуск событий для выборки (значение по умолчанию = 0).
     * @param size          - размер выборки (значение по умолчанию = 10).
     * @param sort          - критерий сортировки
     * @param onlyAvailable - отбор по критерию "только в статусе "Доступные"
     * @return Список событий.
     */

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

        statsClient.sendHit(new EndpointHitDto("ExploreWithMe", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now()));

        List<EventShortDto> events = eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return ResponseEntity.ok(events);
    }


    // TODO swagger

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Запрос события с идентификатором {}", id);

        statsClient.sendHit(new EndpointHitDto("${SPRING_APPLICATION_NAME}", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now()));

        EventFullDto event = eventService.getPublicEventById(id);

        return ResponseEntity.ok(event);
    }
}
