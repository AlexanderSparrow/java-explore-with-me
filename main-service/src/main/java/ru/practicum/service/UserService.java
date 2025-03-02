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
     *
     * @param ids  список ID пользователей (опционально)
     * @param from сколько пользователей пропустить в выводе (опционально)
     * @param size количество в выводе (опционально)
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
     *
     * @param newUserRequest запрос на изменение пользователя админом
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
     *
     * @param userId ID пользователя
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException("Пользователь с id=" + userId + " не найден.", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }
}
