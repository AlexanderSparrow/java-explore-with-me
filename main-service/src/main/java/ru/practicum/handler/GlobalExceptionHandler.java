package ru.practicum.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.exception.AppException;
import ru.practicum.model.ApiError;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ApiError handleAppException(AppException e) {
        return buildErrorResponse(
                e,
                e.getStatus(),
                e.getMessage(),
                "Application-specific exception"
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ApiError handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        return buildErrorResponse(
                e,
                status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR,
                e.getReason() != null ? e.getReason() : "No reason provided",
                "Response status exception"
        );
    }

    @ExceptionHandler(Exception.class)
    public ApiError handleGeneralException(Exception e) {
        return buildErrorResponse(
                e,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage() != null ? e.getMessage() : "Unexpected error occurred",
                "Unhandled exception"
        );
    }

    private ApiError buildErrorResponse(Exception e, HttpStatus status, String message, String reason) {
        log.error("Response Status {}: {}, Reason: {}", status.value(), message, reason, e);
        return new ApiError(
                e.getStackTrace() != null ? List.of(e.getStackTrace()[0].toString()) : Collections.emptyList(),
                message,
                reason,
                status,
                LocalDateTime.now()
        );
    }
}
