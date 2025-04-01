package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.UserStateAction;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateEventService {

    private static final Map<UserStateAction, EventState> statusMap = new HashMap<>(Map.of(
            UserStateAction.CANCEL_REVIEW, EventState.CANCELED,
            UserStateAction.SEND_TO_REVIEW, EventState.PENDING
    ));
    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;

    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Пользователь с id " + userId + " не найден.", HttpStatus.NOT_FOUND));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new AppException(
                        "Категория с id=" + newEventDto.getCategory() + " не найдена.",
                        HttpStatus.NOT_FOUND
                ));

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event = eventRepository.save(event);

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        log.info("Возвращаем {}", eventFullDto);
        return eventFullDto;
    }


    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new AppException("Изменять событие может только его инициатор.", HttpStatus.FORBIDDEN);
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new AppException("Опубликованное событие нельзя редактировать.", HttpStatus.CONFLICT);
        }

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new AppException("Изменять можно только отменённые события или события в ожидании публикации.",
                    HttpStatus.CONFLICT);
        }

        if (updateRequest.getEventDate() != null && updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new AppException("Дата и время события не могут быть раньше, чем через 2 часа от текущего момента.",
                    HttpStatus.BAD_REQUEST);
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

        EventFullDto eventFullDto = eventMapper.toEventFullDto(eventRepository.save(event));
        eventFullDto.setConfirmedRequests(participationRequestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED));
        return eventFullDto;
    }


    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        if (!userService.userExists(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        List<Long> eventsIds = events.stream().map(Event::getId).toList();

        List<Object[]> results = participationRequestRepository.countConfirmedRequestsForEvents(eventsIds);
        Map<Long, Long> confirmedRequestsMap = results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0], // Первый элемент — ID события
                        result -> (Long) result[1]  // Второй элемент — количество подтверждённых заявок
                ));

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено или не принадлежит пользователю id=" + userId, HttpStatus.NOT_FOUND));
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setConfirmedRequests(participationRequestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED)); //TODO
        return eventFullDto;
    }

    /**
     * Метод обновления полей
     */
    private <T> void updateField(T value, Consumer<T> setter) {
        if (Objects.nonNull(value)) setter.accept(value);
    }

}
