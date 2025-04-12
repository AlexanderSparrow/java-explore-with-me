package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EventMapper.class, UserMapper.class})
public interface CommentMapper {
    CommentResponseDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    Comment toEntity(CommentRequestDto dto);

    List<CommentResponseDto> toDtoList(List<Comment> comment);
}