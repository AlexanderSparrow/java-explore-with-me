package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.dto.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateEventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventMapper eventMapper;
    private final ParticipationRequestRepository participationRequestRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new AppException(
                        "Категория с id=" + newEventDto.getCategory() + " не найдена.",
                        HttpStatus.NOT_FOUND
                ));

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiatorId(userId);
        event.setCategoryId(category.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING); // Устанавливаем статус "ожидает публикации"

        event = eventRepository.save(event);

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setRequestModeration(newEventDto.getRequestModeration());

        return eventFullDto;
    }


    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        if (!event.getInitiatorId().equals(userId)) {
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
                    HttpStatus.CONFLICT);
        }
        eventMapper.updateEventFromDto(updateRequest, event);
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        if (!userService.userExists(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }


    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (!userService.userExists(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        if (event.getState() != EventState.PUBLISHED) {
            throw new AppException("Нельзя подать заявку на участие в неопубликованном событии.", HttpStatus.CONFLICT);
        }

        if (event.getInitiatorId().equals(userId)) {
            throw new AppException("Инициатор не может подать заявку на участие в своём событии.", HttpStatus.CONFLICT);
        }

        if (participationRequestRepository.existsByRequesterAndEvent(userId, eventId)) {
            throw new AppException("Запрос на участие уже существует.", HttpStatus.CONFLICT);
        }

        long confirmedRequests = participationRequestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new AppException("Достигнуто максимальное количество участников для события c id: " + eventId + ".",
                    HttpStatus.CONFLICT);
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event.getId())
                .requester(userId)
                .status(Boolean.TRUE.equals(event.getParticipantLimit() > 0) ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        ParticipationRequest savedRequest = participationRequestRepository.save(request);

        return participationRequestMapper.toDto(savedRequest);
    }


    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        if (!event.getInitiatorId().equals(userId)) {
            throw new AppException("Только инициатор может управлять заявками на участие.", HttpStatus.FORBIDDEN);
        }

        List<ParticipationRequest> requests = participationRequestRepository.findAllById(request.getRequestIds());
        if (requests.isEmpty()) {
            throw new AppException("Заявки не найдены.", HttpStatus.NOT_FOUND);
        }

        for (ParticipationRequest participationRequest : requests) {
            if (participationRequest.getStatus() != RequestStatus.PENDING) {
                throw new AppException("Изменять статус можно только у заявок в ожидании.", HttpStatus.CONFLICT);
            }
        }

        long confirmedRequests = participationRequestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new AppException("Достигнут лимит заявок на участие.", HttpStatus.CONFLICT);
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        for (ParticipationRequest participationRequest : requests) {
            if (request.getStatus() == RequestStatus.CONFIRMED) {
                if (confirmedRequests < event.getParticipantLimit() || event.getParticipantLimit() == 0) {
                    participationRequest.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(participationRequest);
                    confirmedRequests++;
                } else {
                    participationRequest.setStatus(RequestStatus.REJECTED);
                    rejected.add(participationRequest);
                }
            } else {
                participationRequest.setStatus(RequestStatus.REJECTED);
                rejected.add(participationRequest);
            }
        }

        participationRequestRepository.saveAll(requests);

        return new EventRequestStatusUpdateResult(
                participationRequestMapper.toDtoList(confirmed),
                participationRequestMapper.toDtoList(rejected)
        );
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        // Заглушка
        return List.of();
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        // Заглушка
        return new ParticipationRequestDto();
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        if (!event.getInitiatorId().equals(userId)) {
            throw new AppException("Только инициатор события может просматривать заявки на участие.", HttpStatus.FORBIDDEN);
        }

        List<ParticipationRequest> requests = participationRequestRepository.findByEvent(eventId);
        return participationRequestMapper.toDtoList(requests);
    }


    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено или не принадлежит пользователю id=" + userId, HttpStatus.NOT_FOUND));

        return eventMapper.toEventFullDto(event);
    }

}
