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
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.enums.EventState;
import ru.practicum.enums.StateAction;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEventService {

    private static final Map<StateAction, EventState> statusMap = new HashMap<>(Map.of(
            StateAction.PUBLISH_EVENT, EventState.PUBLISHED,
            StateAction.REJECT_EVENT, EventState.CANCELED
    ));
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    /**
     * Получение списка событий
     *
     * @param users      список ID пользователей (опционально)
     * @param states     статус событий
     * @param categories категории
     * @param rangeStart начальная дата выборки
     * @param rangeEnd   конечная дата выборки
     * @param from       сколько событий пропустить в выводе (опционально)
     * @param size       количество в выводе (опционально)
     */
    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());

        List<Event> events = eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
                users, states, categories, rangeStart, rangeEnd, pageable
        );

        return eventMapper.toEventFullDtoList(events);
    }

    /**
     * Обновление события администратором
     *
     * @param eventId       ID события, которое меняем
     * @param updateRequest запрос на изменение события
     */
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Событие с id: " + eventId + " не найдено."));
        if (updateRequest.getEventDate() != null && updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new AppException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации.",
                    HttpStatus.BAD_REQUEST);
        }

        if ((event.getState() == EventState.PUBLISHED) && (updateRequest.getStateAction() == StateAction.PUBLISH_EVENT)) {
            throw new AppException("Нельзя публиковать уже опубликованное событие.", HttpStatus.CONFLICT);
        }

        if ((event.getState() == EventState.CANCELED) && (updateRequest.getStateAction() == StateAction.PUBLISH_EVENT)) {
            throw new AppException("Нельзя опубликовать отклоненное событие.", HttpStatus.CONFLICT);
        }

        if ((event.getState() == EventState.PUBLISHED) && (updateRequest.getStateAction() == StateAction.REJECT_EVENT)) {
            throw new AppException("Нельзя отклонить уже опубликованное событие.", HttpStatus.CONFLICT);
        }

        updateField(updateRequest.getTitle(), event::setTitle);
        updateField(updateRequest.getAnnotation(), event::setAnnotation);
        updateField(updateRequest.getDescription(), event::setDescription);
        updateField(updateRequest.getCategory(), event::setCategoryId);
        updateField(updateRequest.getEventDate(), event::setEventDate);
        updateField(updateRequest.getLocation(), event::setLocation);
        updateField(updateRequest.getPaid(), event::setPaid);
        updateField(updateRequest.getParticipantLimit(), event::setParticipantLimit);

        log.debug("Received status: {}", updateRequest.getStateAction());

        event.setState(Optional.ofNullable(updateRequest.getStateAction())
                .map(statusMap::get)
                .orElse(EventState.PENDING));

        log.debug("Mapped event status: {}", event.getState());

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    /**
     * Метод обновления полей
     */
    private <T> void updateField(T value, Consumer<T> setter) {
        if (Objects.nonNull(value)) setter.accept(value);
    }


    public void trackEventView(Long eventId, String ip) {
        EndpointHitDto hit = new EndpointHitDto("ExploreWithMe", "/events/" + eventId, ip, LocalDateTime.now());
        log.info("Отправка статистики: {}", hit);
        statsClient.sendHit(hit);
    }
}
