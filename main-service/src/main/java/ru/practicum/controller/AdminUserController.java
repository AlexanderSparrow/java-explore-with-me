package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * Получение информации о пользователях
     * @param ids  Список ID пользователей (опционально)
     * @param from Количество элементов, которые нужно пропустить
     * @param size Количество элементов в ответе
     * @return Список пользователей
     */
    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Получен запрос на вывод списка пользователей с id{}, from={}, size={}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    /**
     * Добавление нового пользователя
     * @param newUserRequest Данные добавляемого пользователя
     * @return Зарегистрированный пользователь
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerUser(@RequestBody NewUserRequest newUserRequest) {
        log.info("Получен запрос на добавление пользователя {}.", newUserRequest);
        return userService.registerUser(newUserRequest);
    }

    /**
     * Удаление пользователя
     * @param userId ID пользователя
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Получен запрос на удаление пользователя с id: {}.", userId);
        userService.deleteUser(userId);
    }
}
