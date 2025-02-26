package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.CompilationDto;
import ru.practicum.model.Compilation;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {
    CompilationDto toDto(Compilation compilation);
}