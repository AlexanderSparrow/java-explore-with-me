package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.enums.StateAction;
import ru.practicum.enums.EventState;
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

    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        List<Event> events = eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
                users, states, categories, rangeStart, rangeEnd
        );
        return eventMapper.toEventFullDtoList(events);
    }

    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Событие с id: " + eventId + " не найдено."));

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

    private <T> void updateField(T value, Consumer<T> setter) {
        if (Objects.nonNull(value)) setter.accept(value);
    }


    public void trackEventView(Long eventId, String ip) {
        EndpointHitDto hit = new EndpointHitDto("ExploreWithMe", "/events/" + eventId, ip, LocalDateTime.now());
        log.info("Отправка статистики: {}", hit);
        statsClient.sendHit(hit);
    }
}
