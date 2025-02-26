package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.enums.EventState;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicEventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size, getSort(sort));

        List<Event> events = eventRepository.findPublishedEvents(
                text != null ? text.toLowerCase() : null,
                categories, paid, rangeStart != null ? rangeStart : LocalDateTime.now(), rangeEnd, pageable);
        return events.stream()
                .filter(event -> !onlyAvailable || requestRepository.countConfirmedRequests(event.getId()) < event.getParticipantLimit())
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getPublicEventById(Long eventId) {
        Event event = eventRepository.findPublishedEventById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        return eventMapper.toEventFullDto(event);
    }

    private Sort getSort(String sort) {
        return "VIEWS".equalsIgnoreCase(sort) ? Sort.by(Sort.Direction.DESC, "views") : Sort.by(Sort.Direction.ASC, "eventDate");
    }


    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        List<Event> events = eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
                users, states, categories, rangeStart, rangeEnd
        );
        return eventMapper.toEventFullDtoList(events);
    }

    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Событие не найдено."));

        // Применяем обновления
        event.setTitle(updateRequest.getTitle());
        event.setAnnotation(updateRequest.getAnnotation());  // добавляем аннотацию
        event.setDescription(updateRequest.getDescription());
        event.setCategoryId(updateRequest.getCategory());
        event.setEventDate(updateRequest.getEventDate());
        event.setLocation(updateRequest.getLocation());
        event.setPaid(updateRequest.getPaid());
        event.setParticipantLimit(updateRequest.getParticipantLimit());

        // Обработка нового поля status
        if (updateRequest.getStatus() != null) {
            // Обновление состояния на основе status
            switch (updateRequest.getStatus()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    event.setState(EventState.PENDING);
                    break;
            }
        }

        event = eventRepository.save(event);

        return eventMapper.toEventFullDto(event);
    }

    public void trackEventView(Long eventId, String ip) {
        EndpointHitDto hit = new EndpointHitDto("ExploreWithMe", "/events/" + eventId, ip, LocalDateTime.now());
        log.info("Отправка статистики: {}", hit);
        statsClient.sendHit(hit);
    }
}
