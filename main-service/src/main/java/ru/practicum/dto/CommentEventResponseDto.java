package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEventResponseDto {
    private Long id;
    private String text;
    private UserForCommentDto author;
    private LocalDateTime created;
    private LocalDateTime updated;
}
