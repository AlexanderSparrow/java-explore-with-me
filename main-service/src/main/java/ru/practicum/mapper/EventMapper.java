package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventFullDto toEventFullDto(Event event);

    List<EventFullDto> toEventFullDtoList(List<Event> events);

    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    EventShortDto toEventShortDto(Event event);

    List<EventShortDto> toEventShortDtoList(List<Event> events);

    @Mapping(source = "category", target = "categoryId")
    @Mapping(target = "initiatorId", ignore = true) // Заполняем вручную в сервисе
    @Mapping(target = "state", constant = "PENDING")
    Event toEntity(NewEventDto dto);
}
