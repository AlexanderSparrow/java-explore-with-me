package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParticipationRequestMapper {
    public ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent(),
                request.getRequester(),
                request.getStatus(),
                request.getCreated()
        );
    }

    public ParticipationRequest toEntity(ParticipationRequestDto dto) {
        if (dto == null) {
            return null;
        }
        ParticipationRequest request = new ParticipationRequest();
        request.setId(dto.getId());
        request.setEvent(dto.getEvent());
        request.setRequester(dto.getRequester());
        request.setStatus(dto.getStatus());
        request.setCreated(dto.getCreated());
        return request;
    }

    public List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}