package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;


    @Override
    public List<User> getUserList() {
        return userStorage.getUserList();
    }

    @Override
    public User createUser(User user) {
        if (userStorage.existByEmail(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User newUser) {
        Long newUserId = newUser.getId();
        if (newUserId == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!userStorage.containsUserById(newUserId)) {
            throw new NotFoundException("Пользователь с id = " + newUserId + " не найден");
        }

        if (userStorage.existByEmail(newUser.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        return userStorage.updateUser(newUser);
    }


    @Override
    public void addFriend(long id, long friendId) {
        if (!userStorage.containsUserById(id)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        if (!userStorage.containsUserById(friendId)) {
            throw new NotFoundException("Пользователь c id = " + friendId + " не найден");
        }
        userStorage.addFriend(id, friendId);
    }


    @Override
    public void deleteFriend(long id, long friendId) {
        if (!userStorage.containsUserById(id)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        if (!userStorage.containsUserById(friendId)) {
            throw new NotFoundException("Пользователь c id = " + friendId + " не найден");
        }
        userStorage.deleteFriend(id, friendId);
    }


    @Override
    public List<User> getFriendsList(long id) {
        if (!userStorage.containsUserById(id)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        return userStorage.getFriendsList(id);
    }


    @Override
    public List<User> getCommonFriendsList(long id, long otherId) {
        if (!userStorage.containsUserById(id)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        if (!userStorage.containsUserById(otherId)) {
            throw new NotFoundException("Пользователь c id = " + otherId + " не найден");
        }

        return userStorage.getCommonFriendsList(id, otherId);
    }

}
