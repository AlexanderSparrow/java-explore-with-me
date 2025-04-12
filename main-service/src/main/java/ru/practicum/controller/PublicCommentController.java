package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.service.PublicCommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class PublicCommentController {

    private final PublicCommentService publicCommentService;

    /**
     * Получить список комментариев к событию
     *
     * @param eventId - идентификатор события
     */
    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getEventComments(@PathVariable Long eventId) {
        log.info("GET /events/{}/comments - получаем комментарии", eventId);
        return publicCommentService.getEventComments(eventId);
    }

    /**
     * Посмотреть комментарий
     *
     * @param commentId - идентификатор комментария
     * @return Обновленный - комментарий
     */
    @GetMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto getComment(@PathVariable Long commentId) {
        log.info("GET /comments/{} - просмотр комментария", commentId);
        return publicCommentService.getComment(commentId);
    }
}