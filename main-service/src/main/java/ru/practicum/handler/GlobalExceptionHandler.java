package ru.practicum.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ApiError handleAppException(AppException e) {
        return buildErrorResponse(e, e.getStatus());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ApiError handleResponseStatusException(ResponseStatusException e) {
        return buildErrorResponse(e, (HttpStatus) e.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ApiError handleGeneralException(Exception e) {
        return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiError buildErrorResponse(Exception e, HttpStatus status) {
        log.error("Response Status {}: {}", status.value(), e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        return new ApiError(
                List.of(sw.toString()),
                e.getMessage(),
                status.getReasonPhrase(),
                status,
                LocalDateTime.now()
        );
    }
}
