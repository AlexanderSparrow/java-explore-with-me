package ru.practicum.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    /**
     * Создание новой подборки
     */
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new AppException("Подборка с названием ='" + newCompilationDto.getTitle() + "' уже существует.", HttpStatus.CONFLICT);
        }

        // Загружаем события по ID
        Set<Event> events = newCompilationDto.getEvents() == null
                ? Collections.emptySet()
                : new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));

        Compilation compilation = Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned() != null && newCompilationDto.getPinned())
                .events(events)
                .build();

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Сохранили подборку: {}.", savedCompilation);
        return compilationMapper.toDto(savedCompilation);
    }

    /**
     * Получение подборок
     */
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable);
        } else {
            compilations = compilationRepository.findByPinned(pinned, pageable);
        }

        return compilations.stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new AppException("Подборка с id=" + compId + " не найдена.", HttpStatus.NOT_FOUND));
        log.info("Возвращаем подборку: {}.", compilation);
        return compilationMapper.toDto(compilation);
    }

    /**
     * Удаление подборки
     */
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new AppException("Подборка с id=" + compId + " не найдена.", HttpStatus.NOT_FOUND);
        }
        compilationRepository.deleteById(compId);
        log.info("Подборка с id={} удалена.", compId);
    }

    /**
     * Обновление подборки
     */
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new AppException("Подборка с id=" + compId + " не найдена.", HttpStatus.NOT_FOUND));

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(request.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка с id={} обновлена.", compId);
        return compilationMapper.toDto(updatedCompilation);
    }
}
