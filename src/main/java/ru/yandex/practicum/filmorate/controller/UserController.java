package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long counterId = 0;

    private long getNextId() {
        return ++counterId;
    }

    @GetMapping
    public List<User> getUsersList() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на добавление пользователя: {}", user);
        for (User value : users.values()) {
            if (value.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Добавление пользователя: {} - закончено, присвоен id: {}", user, user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на обновление данных пользователя c id: {}", newUser.getId());
        Long newUserId = newUser.getId();

        if (newUserId == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (!users.containsKey(newUserId)) {
            throw new NotFoundException("Пользователь с id = " + newUserId + " не найден");
        } else {
            for (User value : users.values()) {
                if (value.getEmail().equals(newUser.getEmail()) && !value.getId().equals(newUserId)) {
                    throw new DuplicatedDataException("Этот имейл уже используется");
                }
            }

            User oldUser = users.get(newUserId);
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            if (newUser.getName() == null || newUser.getName().isBlank()) {
                oldUser.setName(oldUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }
            oldUser.setBirthday(newUser.getBirthday());
            log.info("Обновление данных пользователя: {} - закончено", oldUser);
            return oldUser;
        }
    }
}
