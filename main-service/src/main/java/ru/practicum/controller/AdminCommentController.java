package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.service.AdminCommentService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    /**
     * Получение событий администратором
     *
     * @param usersIds    - Список идентификаторов пользователей (опционально)
     * @param eventsIds   - список идентификаторов событий (опционально)
     * @param commentsIds - список идентификаторов комментариев
     * @param rangeStart  - начало диапазона дат (опционально)
     * @param rangeEnd    - конец диапазона дат (опционально)
     * @param from        - Количество элементов, которые нужно пропустить (опционально)
     * @param size        - Количество элементов в ответе (опционально, по умолчанию)
     * @return Список комментариев по параметрам
     */
    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> usersIds,
            @RequestParam(required = false) List<Long> eventsIds,
            @RequestParam(required = false) List<Long> commentsIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Получен запрос комментариев, содержащих текст {} от пользователей {} к событиям {} с идентификаторами {} в диапазоне дат с:{} по: {} " +
                "начиная с номера: {}, всего в количестве: {}", text, usersIds, eventsIds, commentsIds, rangeStart, rangeEnd, from, size);
        List<CommentResponseDto> comments = adminCommentService.getComments(text, usersIds, eventsIds, commentsIds, rangeStart, rangeEnd, from, size);
        log.info("Список событий: {}.", comments);
        return ResponseEntity.ok(comments);
    }

    /**
     * Обновить комментарий
     *
     * @param commentId         - идентификатор комментария
     * @param commentRequestDto - изменения в комментарий
     * @return Обновленный - комментарий
     */
    @PatchMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto updateComment(@PathVariable Long commentId,
                                            @RequestBody CommentRequestDto commentRequestDto) {
        log.info("PATCH /admin/comments/{} - обновляем комментарий: {}", commentId, commentRequestDto);
        return adminCommentService.updateComment(commentId, commentRequestDto);
    }

    /**
     * Удаление комментария
     *

     * @param commentId - идентификатор комментария
     */
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("DELETE /admin/comments/{} - удаляем комментарий", commentId);
        adminCommentService.deleteComment(commentId);
    }
}