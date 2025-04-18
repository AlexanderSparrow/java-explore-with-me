package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.UserStateAction;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.practicum.service.EventFullDtoCreator.feelViewsField;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateEventService {

    private static final Map<UserStateAction, EventState> statusMap = Map.of(
            UserStateAction.CANCEL_REVIEW, EventState.CANCELED,
            UserStateAction.SEND_TO_REVIEW, EventState.PENDING
    );

    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Пользователь с id " + userId + " не найден.", HttpStatus.NOT_FOUND));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new AppException("Категория с id=" + newEventDto.getCategory() + " не найдена.", HttpStatus.NOT_FOUND));

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());

        event = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(event);
        List<CommentEventResponseDto> commentsDto = (commentMapper.toCommentEventResponseDto(commentRepository.findAllByEvent_Id(event.getId())));
        dto.setComments(commentsDto);
        return dto;
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
            throw new AppException("Изменять можно только отменённые события или события в ожидании публикации.", HttpStatus.CONFLICT);
        }

        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new AppException("Дата и время события не могут быть раньше, чем через 2 часа от текущего момента.", HttpStatus.BAD_REQUEST);
        }

        updateField(updateRequest.getTitle(), event::setTitle);
        updateField(updateRequest.getAnnotation(), event::setAnnotation);
        updateField(updateRequest.getDescription(), event::setDescription);

        if (updateRequest.getCategory() != null) {
            var category = categoryRepository.getCategoryById(updateRequest.getCategory());
            if (category == null) {
                throw new AppException("Категория не найдена", HttpStatus.NOT_FOUND);
            }
            event.setCategory(category);
        }

        updateField(updateRequest.getEventDate(), event::setEventDate);
        updateField(updateRequest.getLocation(), event::setLocation);
        updateField(updateRequest.getPaid(), event::setPaid);
        updateField(updateRequest.getParticipantLimit(), event::setParticipantLimit);

        event.setState(Optional.ofNullable(updateRequest.getStateAction())
                .map(statusMap::get)
                .orElse(EventState.PENDING));

        Event updated = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(updated);
        dto.setConfirmedRequests(participationRequestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED));
        return dto;
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "createdOn"));

        if (!userService.userExists(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        if (events.isEmpty()) return List.of();

        // Подсчёт подтверждённых заявок
        Map<Long, Long> confirmedRequestsMap = getconfirmedRequestsMap(events);

        // Подготовка URI -> ID и дат
        Map<String, Event> uriToEventMap = events.stream()
                .collect(Collectors.toMap(e -> "/events/" + e.getId(), e -> e));

        // Запрос статистики
        ViewsStatsRequest statsRequest = ViewsStatsRequest.builder()
                .uris(uriToEventMap.keySet())
                .unique(true)
                .build();

        List<ViewStats> stats = statsClient.getStats(List.of(statsRequest));

        Map<String, Long> viewsMap = stats.stream()
                .collect(Collectors.toMap(ViewStats::getUri, ViewStats::getHits));

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    List<CommentEventResponseDto> commentsDto = (commentMapper.toCommentEventResponseDto(commentRepository.findAllByEvent_Id(event.getId())));
                    dto.setComments(commentsDto);
                    return dto;
                }).collect(Collectors.toList());
    }

    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено или не принадлежит пользователю id=" + userId, HttpStatus.NOT_FOUND));

        return feelViewsField(eventId, event, commentMapper, commentRepository, eventMapper, participationRequestRepository, statsClient);
    }

    private Map<Long, Long> getconfirmedRequestsMap(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> map = participationRequestRepository
                .countConfirmedRequestsForEvents(eventIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        return map;
    }

    private <T> void updateField(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }
}
