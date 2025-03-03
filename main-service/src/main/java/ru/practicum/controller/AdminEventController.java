package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.enums.EventState;
import ru.practicum.service.AdminEventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService adminEventService;

    /**
     * Получение событий администратором
     *
     * @param users      Список ID пользователей (опционально)
     * @param states     список ID статусов (опционально)
     * @param categories список категорий (опционально)
     * @param rangeStart начало диапазона дат
     * @param rangeEnd   конец диапазона дат
     * @param from       Количество элементов, которые нужно пропустить
     * @param size       Количество элементов в ответе
     * @return Список событий по параметрам
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<EventFullDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Получен запрос событий пользователей {} со статусами {} из категорий {} в диапазоне дат с:{} по: {} " +
                "начиная с номера: {}, всего в количестве: {}", users, states, categories, rangeStart, rangeEnd, from, size);
        List<EventFullDto> events = adminEventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
        log.info("Список событий: {}.", events);
        return ResponseEntity.ok(events);
    }

    /**
     * Редактирование события администратором
     *
     * @param eventId       ID события
     * @param updateRequest Запрос на обновление
     * @return Обновленное событие
     */
    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EventFullDto> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateRequest) {
        log.info("Получен запрос на изменение события с id: {} на {}", eventId, updateRequest);
        EventFullDto updatedEvent = adminEventService.updateEvent(eventId, updateRequest);
        log.info("Обновленное событие: {}", updatedEvent);
        return ResponseEntity.ok(updatedEvent);
    }
}
