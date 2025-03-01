package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.exception.AppException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Получение списка пользователей
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (from < 0 || size <= 0) {
            throw new AppException("Некорректные значения параметров пагинации.", HttpStatus.BAD_REQUEST);
        }

        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAllWithPagination(from, size);
        } else {
            users = userRepository.findByIdIn(ids, from, size);
        }

        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Регистрация нового пользователя
     */
    @Transactional
    public UserDto registerUser(NewUserRequest newUserRequest) {
        if (newUserRequest.getName() == null || newUserRequest.getName().isBlank()) {
            throw new AppException("Field: name. Error: must not be blank. Value: null", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new AppException("Пользователь с таки e-mail уже существует.", HttpStatus.CONFLICT);
        }

        User user = userMapper.fromNewUserRequest(newUserRequest);
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    /**
     * Удаление пользователя
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }
}
