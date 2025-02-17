package ru.practicum.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EventFullDto extends EventShortDto {
    private String description;
    private UserShortDto initiator;
    private String location;
    private Boolean paid;
}
