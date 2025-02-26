package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.*;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventMapper eventMapper;

    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiatorId(userId);
        event.setCreatedOn(LocalDateTime.now());
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto updateEvent(Long userId, Long eventId, EventFullDto eventFullDto) {
        // Заглушка
        return new EventFullDto();
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        // Заглушка
        return List.of();
    }

    public EventFullDto getUserEventById(Long userId, Long eventId) {
        // Заглушка
        return new EventFullDto();
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        // Заглушка
        return List.of();
    }

    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        // Заглушка
        return new EventRequestStatusUpdateResult();
    }

    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        // Заглушка
        return new ParticipationRequestDto();
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        // Заглушка
        return List.of();
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        // Заглушка
        return new ParticipationRequestDto();
    }
}
