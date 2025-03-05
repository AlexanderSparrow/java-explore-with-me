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
import ru.practicum.exception.AppException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateEventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;

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
        log.info("Возвращаем {}", eventFullDto);
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
                    HttpStatus.BAD_REQUEST);
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

    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено или не принадлежит пользователю id=" + userId, HttpStatus.NOT_FOUND));

        return eventMapper.toEventFullDto(event);
    }

}
