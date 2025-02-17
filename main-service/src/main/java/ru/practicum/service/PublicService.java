package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;

import java.util.List;

@Service
public class PublicService {

    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        // Заглушка
        return List.of();
    }

    public EventFullDto getEventById(Long id) {
        // Заглушка
        return new EventFullDto();
    }

    public List<CategoryDto> getCategories() {
        // Заглушка
        return List.of();
    }

    public List<CompilationDto> getCompilations(Boolean pinned) {
        // Заглушка
        return List.of();
    }
}
