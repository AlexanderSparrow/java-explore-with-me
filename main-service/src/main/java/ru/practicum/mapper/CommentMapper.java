package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dto.CommentEventResponseDto;
import ru.practicum.dto.CommentResponseDto;
import ru.practicum.model.Comment;
import ru.practicum.model.User;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EventMapper.class, UserMapper.class})
public interface CommentMapper {
    CommentResponseDto toDto(Comment comment);

    @Mapping(source = "id", target = "id", qualifiedByName = "mapUser")
    List<CommentEventResponseDto> toCommentEventResponseDto(List<Comment> comments);

    List<CommentResponseDto> toDtoList(List<Comment> comment);

    // Методы преобразования Long → User
    @Named("mapUser")
    default User user (Long id) {
        return id == null ? null : User.builder().id(id).build();
    }
}