package ru.practicum.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ApiError {
    private HttpStatus status;
    private String error;
    private String message;
    private String stackTrace;
}
