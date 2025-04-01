package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EventFullDto;
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

    private static final Map<StateAction, EventState> statusMap = new HashMap<>(Map.of(
            StateAction.PUBLISH_EVENT, EventState.PUBLISHED,
            StateAction.REJECT_EVENT, EventState.CANCELED
    ));
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository participationRequestRepository;

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
        if (from < 0 || size <= 0) {
            throw new AppException("Некорректные параметры пагинации", HttpStatus.BAD_REQUEST);
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
                pageable
        );

        List<Long> eventsIds = events.stream().map(Event::getId).toList();

        List<Object[]> results = participationRequestRepository.countConfirmedRequestsForEvents(eventsIds);
        Map<Long, Long> confirmedRequestsMap = results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0], // Первый элемент — ID события
                        result -> (Long) result[1]  // Второй элемент — количество подтверждённых заявок
                ));

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event);
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
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
        updateField(categoryRepository.getCategoryById(updateRequest.getCategory()), event::setCategory);
        updateField(updateRequest.getEventDate(), event::setEventDate);
        updateField(updateRequest.getLocation(), event::setLocation);
        updateField(updateRequest.getPaid(), event::setPaid);
        updateField(updateRequest.getParticipantLimit(), event::setParticipantLimit);

        event.setState(Optional.ofNullable(updateRequest.getStateAction())
                .map(statusMap::get)
                .orElse(EventState.PENDING));

        if (event.getState().equals(EventState.PUBLISHED)) {
            event.setPublishedOn(LocalDateTime.now());
        }

        EventFullDto eventFullDto = eventMapper.toEventFullDto(eventRepository.save(event));
        eventFullDto.setConfirmedRequests(participationRequestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED));
        return eventFullDto;
    }

    /**
     * Метод обновления полей
     */
    private <T> void updateField(T value, Consumer<T> setter) {
        if (Objects.nonNull(value)) setter.accept(value);
    }

}
