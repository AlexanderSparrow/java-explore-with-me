package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentResponseDto createComment(Long userId, Long eventId, CommentRequestDto commentRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));


        Comment comment = Comment.builder()
                .text(commentRequestDto.getText())
                .author(user)
                .created(LocalDateTime.now())
                .event(event)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDto(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getEventComments(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException("Событие с id=" + eventId + " не найдено.", HttpStatus.NOT_FOUND));

        List<Comment> comments = commentRepository.findAllByEvent_Id(eventId);
        return commentMapper.toDtoList(comments);
    }

    public CommentResponseDto updateComment(Long userId, Long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Комментарий с id: " + commentId + " не найден.", HttpStatus.NOT_FOUND));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AppException("Можно редактировать только собственные комментарии.", HttpStatus.FORBIDDEN);
        }

        comment.setText(commentRequestDto.getText());
        comment.setUpdated(LocalDateTime.now());

        return commentMapper.toDto(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Комментарий с id: " + commentId + " не найден.", HttpStatus.NOT_FOUND));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AppException("Можно редактировать только собственные комментарии.", HttpStatus.FORBIDDEN);
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий id={} удалён пользователем id={}", commentId, userId);
    }

    public CommentResponseDto getComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Комментарий с id: " + commentId + " не найден.", HttpStatus.NOT_FOUND));

        return commentMapper.toDto(comment);
    }

    public List<CommentResponseDto> getUserComment(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findAllByAuthor_Id(userId);
        return commentMapper.toDtoList(comments);
    }
}
