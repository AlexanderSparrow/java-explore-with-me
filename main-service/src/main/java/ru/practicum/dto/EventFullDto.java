package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class EventFullDto extends EventShortDto {

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @NotBlank
    private String description;

    @NotNull
    private Location location;

    @PositiveOrZero
    private int participantLimit = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    @NotNull
    private Boolean requestModeration = true;

    @NotNull
    private EventState state;
}
