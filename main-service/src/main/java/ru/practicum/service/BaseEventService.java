package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.repository.ParticipationRequestRepository;

import java.util.Objects;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public abstract class BaseEventService {
    protected final ParticipationRequestRepository participationRequestRepository;



    /**
     * Метод обновления полей
     */
    protected  <T> void updateField(T value, Consumer<T> setter) {
        if (Objects.nonNull(value)) setter.accept(value);
    }
}
