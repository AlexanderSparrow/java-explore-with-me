package ru.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.enums.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    @NotNull
    private List<Long> requestIds;

    @NotNull
    private RequestStatus status;
}
