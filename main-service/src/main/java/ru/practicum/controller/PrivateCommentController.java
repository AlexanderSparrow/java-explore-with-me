package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long eventId,
                                            @RequestBody CommentRequestDto commentRequestDto) {
        log.info("Получен запрос пользователя  с id {} на создание комментария к событию с id {}", userId, eventId);
        return commentService.createComment(userId, commentRequestDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getEventComments(@PathVariable Long eventId) {
        log.info("Получен запрос списка комментариев события с id {}.", eventId);
        return commentService.getEventComment(eventId);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto cancelRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long commentId,
                                            @RequestBody CommentRequestDto commentRequestDto) {
        log.info("Получен запрос на редактирование комментария c id {} пользователем с id {}&", commentId, userId);
        return commentService.updateComment(userId, commentId, commentRequestDto);
    }
}
