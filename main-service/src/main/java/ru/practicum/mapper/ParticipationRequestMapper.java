package ru.practicum.mapper;

import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

public class ParticipationRequestMapper {
    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent(),
                request.getRequester(),
                request.getStatus(),
                request.getCreated()
        );
    }

    public static ParticipationRequest toEntity(ParticipationRequestDto dto) {
        ParticipationRequest request = new ParticipationRequest();
        request.setId(dto.getId());
        request.setEvent(dto.getEvent());
        request.setRequester(dto.getRequester());
        request.setStatus(dto.getStatus());
        request.setCreated(dto.getCreated());
        return request;
    }
}
