package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicCommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;


    @Transactional(readOnly = true)
    public List<CommentResponseDto> getEventComments(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        List<Comment> comments = commentRepository.findAllByEvent_Id(eventId);
        return commentMapper.toDtoList(comments);
    }

    public CommentResponseDto getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Комментарий с id: " + commentId + " не найден.", HttpStatus.NOT_FOUND));

        return commentMapper.toDto(comment);
    }

}
