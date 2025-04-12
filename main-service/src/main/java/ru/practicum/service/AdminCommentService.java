package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Комментарий с id: " + commentId + " не найден.", HttpStatus.NOT_FOUND));

        comment.setText(commentRequestDto.getText());
        comment.setUpdated(LocalDateTime.now());

        return commentMapper.toDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Комментарий с id: " + commentId + " не найден.", HttpStatus.NOT_FOUND));

        commentRepository.deleteById(commentId);
        log.info("Комментарий id={} удалён администратором", commentId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(String text, List<Long> usersIds, List<Long> eventsIds, List<Long> commentsIds,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                int from, int size) {

        if (from < 0 || size <= 0) {
            throw new AppException("Некорректные параметры пагинации.", HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());

        boolean filterText = text != null && !text.isBlank() && text.equals("0");
        boolean filterUsers = usersIds != null && !usersIds.isEmpty() && !(usersIds.size() == 1 && usersIds.getFirst() == 0);
        boolean filterEvents = eventsIds != null && !eventsIds.isEmpty() && !(eventsIds.size() == 1 && eventsIds.getFirst() == 0);
        boolean filterComments = commentsIds != null && !commentsIds.isEmpty() && !(commentsIds.size() == 1 && commentsIds.getFirst() == 0);
        boolean filterDates = rangeStart != null && rangeEnd != null && rangeStart.isBefore(rangeEnd);

        List<Comment> comments = commentRepository.findWithFilters(

                filterUsers ? usersIds : null,
                filterEvents ? eventsIds : null,
                filterComments ? commentsIds : null,
                filterDates ? rangeStart : null,
                filterDates ? rangeEnd : null,
                filterText ? text : null,
                pageable
        );

        return commentMapper.toDtoList(comments);

    }

}
