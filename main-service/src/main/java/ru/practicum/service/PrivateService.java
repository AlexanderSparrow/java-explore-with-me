package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.dto.*;

import java.util.List;

@Service
public class PrivateService {

    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        // Заглушка
        return new EventFullDto();
    }

    public EventFullDto updateEvent(Long userId, Long eventId, EventUpdateDto eventUpdateDto) {
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
