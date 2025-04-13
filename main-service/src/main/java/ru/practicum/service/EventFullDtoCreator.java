package ru.practicum.service;

import ru.practicum.StatsClient;
import ru.practicum.dto.CommentEventResponseDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.ViewsStatsRequest;
import ru.practicum.enums.RequestStatus;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.ParticipationRequestRepository;

import java.util.List;

public class EventFullDtoCreator {


    static EventFullDto feelViewsField(Long eventId,
                                       Event event,
                                       CommentMapper commentMapper,
                                       CommentRepository commentRepository,
                                       EventMapper eventMapper,
                                       ParticipationRequestRepository requestRepository,
                                       StatsClient statsClient) {
        EventFullDto dto = eventMapper.toEventFullDto(event);
        dto.setConfirmedRequests(requestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED));
        List<CommentEventResponseDto> commentsDto = (commentMapper.toCommentEventResponseDto(commentRepository.findAllByEvent_Id(event.getId())));
        dto.setComments(commentsDto);

        ViewsStatsRequest statsRequest = ViewsStatsRequest.builder()
                .uri("/events/" + eventId)
                .unique(true)
                .build();

        List<ViewStats> stats = statsClient.getStats(List.of(statsRequest));
        dto.setViews(stats.isEmpty() ? 0L : stats.getFirst().getHits());

        return dto;
    }
}
