package ru.practicum.dto;

import lombok.Data;

@Data
public class ParticipationRequestDto {
    private Long id;
    private Long event;
    private Long requester;
    private String status;
}
