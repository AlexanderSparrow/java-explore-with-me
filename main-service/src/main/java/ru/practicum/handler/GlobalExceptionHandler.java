package ru.practicum.handler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.exception.AppException;
import ru.practicum.dto.ApiError;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Глобальный обработчик исключений для всего приложения.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключения типа {@link MissingServletRequestParameterException}.
     *
     * @param e объект исключения
     * @return объект {@link ApiError} с описанием ошибки
     */
    @Operation(summary = "Обработка ошибок запроса с отсутствующим обязательным query-параметром",
            description = "Обрабатывает исключения типа MissingServletRequestParameterException")
    @ApiResponse(responseCode = "4xx", description = "Ошибка, связанная с отсутствием обязательного query-параметра")
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParam(MissingServletRequestParameterException e) {
        return buildErrorResponse(
                e,
                HttpStatus.BAD_REQUEST,
                "Missing required request parameter: " + e.getParameterName(),
                "Request parameter is missing"
        );
    }

    /**
     * Обрабатывает исключения типа {@link AppException}.
     *
     * @param e объект исключения
     * @return объект {@link ApiError} с описанием ошибки
     */
    @Operation(summary = "Обработка кастомных исключений", description = "Обрабатывает исключения уровня приложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
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

    /**
     * Обрабатывает исключения типа {@link ResponseStatusException}.
     *
     * @param e объект исключения
     * @return объект {@link ApiError} с описанием ошибки
     */
    @Operation(summary = "Обработка ошибок с кодами статуса", description = "Обрабатывает исключения типа ResponseStatusException")
    @ApiResponse(responseCode = "4xx", description = "Ошибка, связанная с некорректным статус-кодом")
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

    /**
     * Обрабатывает ошибки валидации (невалидные аргументы метода).
     *
     * @param e объект исключения
     * @return объект {@link ApiError} с описанием ошибки
     */
    @Operation(summary = "Обработка ошибок валидации", description = "Возвращает список ошибок валидации")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
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

    /**
     * Обрабатывает любые неожиданные исключения.
     *
     * @param e объект исключения
     * @return объект {@link ApiError} с описанием ошибки
     */
    @Operation(summary = "Обработка неожиданных ошибок", description = "Обрабатывает все остальные исключения")
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    @ExceptionHandler(Exception.class)
    public ApiError handleGeneralException(Exception e) {
        return buildErrorResponse(
                e,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage() != null ? e.getMessage() : "Unexpected error occurred",
                "Unhandled exception"
        );
    }

    /**
     * Формирует объект ошибки.
     *
     * @param e       исключение
     * @param status  HTTP статус ошибки
     * @param message сообщение об ошибке
     * @param reason  причина ошибки
     * @return объект {@link ApiError}
     */
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
