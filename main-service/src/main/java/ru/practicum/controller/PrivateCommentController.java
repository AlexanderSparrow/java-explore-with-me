package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.service.PrivateCommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final PrivateCommentService privateCommentService;

    /**
     * Добавить комментарий
     *
     * @param userId            - идентификатор автора комментария
     * @param eventId           - идентификатор события
     * @param commentRequestDto - содержание комментария
     * @return новый комментарий
     */
    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto createComment(@PathVariable Long userId,
                                            @PathVariable Long eventId,
                                            @RequestBody CommentRequestDto commentRequestDto) {
        log.info("POST /users/{}/events/{}/comments - создаём комментарий: {}", userId, eventId, commentRequestDto);
        return privateCommentService.createComment(userId, eventId, commentRequestDto);
    }

    /**
     * Получить список комментариев к событию
     *
     * @param userId  - идентификатор пользователя
     * @param eventId - идентификатор события
     */
    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getEventComments(@PathVariable Long userId,
                                                     @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/comments - получаем комментарии", userId, eventId);
        return privateCommentService.getEventComments(eventId);
    }

    /**
     * Обновить комментарий
     *
     * @param userId            - идентификатор пользователя
     * @param eventId           - идентификатор события
     * @param commentId         - идентификатор комментария
     * @param commentRequestDto - изменения в комментарий
     * @return Обновленный - комментарий
     */
    @PatchMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto updateComment(@PathVariable Long userId,
                                            @PathVariable Long eventId,
                                            @PathVariable Long commentId,
                                            @RequestBody CommentRequestDto commentRequestDto) {
        log.info("PATCH /users/{}/events/{}/comments/{} - обновляем комментарий: {}", userId, eventId, commentId, commentRequestDto);
        return privateCommentService.updateComment(userId, commentId, commentRequestDto);
    }

    /**
     * Удаление комментария
     *
     * @param userId    - идентификатор пользователя
     * @param eventId   - идентификатор события
     * @param commentId - идентификатор комментария
     */
    @DeleteMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long eventId,
                              @PathVariable Long commentId) {
        log.info("DELETE /users/{}/events/{}/comments/{} - удаляем комментарий", userId, eventId, commentId);
        privateCommentService.deleteComment(userId, commentId);
    }

    /**
     * Посмотреть комментарий
     *
     * @param userId    - идентификатор пользователя
     * @param eventId   - идентификатор события
     * @param commentId - идентификатор комментария
     * @return Обновленный - комментарий
     */
    @GetMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto getComment(@PathVariable Long userId,
                                         @PathVariable Long eventId,
                                         @PathVariable Long commentId) {
        log.info("GET /users/{}/events/{}/comments/{} - просмотр комментария", userId, eventId, commentId);
        return privateCommentService.getComment(userId, commentId);
    }


    /**
     * Посмотреть все комментарии пользователя
     *
     * @param userId - идентификатор пользователя
     * @return Список комментариев
     */
    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getUserComments(@PathVariable Long userId) {
        log.info("GET /users/{}/comments - просмотр комментария", userId);
        return privateCommentService.getUserComment(userId);
    }
}