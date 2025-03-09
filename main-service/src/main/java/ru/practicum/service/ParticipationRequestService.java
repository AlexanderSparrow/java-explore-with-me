package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationRequestService {

    private final UserService userService;
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;
    private final EventRepository eventRepository;

    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (!userService.userExists(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        if (event.getState() != EventState.PUBLISHED) {
            throw new AppException("Нельзя подать заявку на участие в неопубликованном событии.", HttpStatus.CONFLICT);
        }

        if (event.getInitiator().getId().equals(userId)) {
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

        if (!event.getInitiator().getId().equals(userId)) {
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
        if (!userService.userExists(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }
        List<ParticipationRequest> requests = participationRequestRepository.findByRequester(userId);
        return requests.stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        if (!userService.userExists(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }

        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException("Запрос на участие с id=" + requestId + " не найден.", HttpStatus.NOT_FOUND));

        participationRequest.setStatus(RequestStatus.CANCELED);

        participationRequestRepository.save(participationRequest);

        return participationRequestMapper.toDto(participationRequest);
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new AppException("Только инициатор события может просматривать заявки на участие.", HttpStatus.FORBIDDEN);
        }

        List<ParticipationRequest> requests = participationRequestRepository.findByEvent(eventId);
        return participationRequestMapper.toDtoList(requests);
    }

}
