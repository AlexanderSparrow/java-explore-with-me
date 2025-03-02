package ru.practicum.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.exception.AppException;
import ru.practicum.model.ApiError;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return buildErrorResponse(
                e,
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации",
                errors.toString()
        );
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleAppException(AppException e) {
        ApiError apiError = buildErrorResponse(
                e,
                e.getStatus(),
                e.getMessage(),
                "Application-specific exception"
        );
        return ResponseEntity.status(e.getStatus()).body(apiError);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ApiError handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        return buildErrorResponse(
                e,
                status,
                e.getReason() != null ? e.getReason() : "Unexpected error",
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

        List<String> errorStack = e.getStackTrace() != null ?
                Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList() :
                Collections.emptyList();

        return new ApiError(
                errorStack,
                message,
                reason,
                status,
                LocalDateTime.now()
        );
    }
}
