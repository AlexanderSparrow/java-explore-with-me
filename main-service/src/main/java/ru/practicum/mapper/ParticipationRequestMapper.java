package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {
    ParticipationRequestDto toDto(ParticipationRequest request);

    ParticipationRequest toEntity(ParticipationRequestDto dto);

    List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests);
}