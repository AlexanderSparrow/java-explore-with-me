package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(source = "category", target = "category") // Маппинг категории
    @Mapping(source = "initiator", target = "initiator") // Маппинг инициатора
    EventFullDto toEventFullDto(Event event);

    List<EventFullDto> toEventFullDtoList(List<Event> events);

    @Mapping(source = "category", target = "category") // Маппинг категории
    @Mapping(source = "initiator", target = "initiator") // Маппинг инициатора
    EventShortDto toEventShortDto(Event event);

    List<EventShortDto> toEventShortDtoList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(source = "category", target = "category", qualifiedByName = "mapCategory") // Переводим Long → Category
    Event toEntity(NewEventDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true) // Добавляем, чтобы избежать ошибки
    @Mapping(target = "state", ignore = true)
    @Mapping(source = "category", target = "category", qualifiedByName = "mapCategory")
    void updateEventFromDto(UpdateEventUserRequest dto, @MappingTarget Event event);

    // Методы преобразования Long → Category / Long → User
    @Named("mapCategory")
    default Category mapCategory(Long id) {
        return id == null ? null : Category.builder().id(id).build();
    }

    @Named("mapUser")
    default User mapUser(Long id) {
        return id == null ? null : User.builder().id(id).build();
    }
}
