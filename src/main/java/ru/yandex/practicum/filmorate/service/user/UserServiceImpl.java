package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public List<User> getUserList() {
        return userRepository.getUserList();
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existByEmail(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userRepository.createUser(user);
    }

    @Override
    public User updateUser(User newUser) {
        Long newUserId = newUser.getId();
        if (newUserId == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!userRepository.containsUserById(newUserId)) {
            throw new NotFoundException("Service: Пользователь с id = " + newUserId + " не найден");
        }

        if (userRepository.existByEmail(newUser.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        return userRepository.updateUser(newUser);
    }


    @Override
    public void addFriend(long id, long friendId) {
        if (!userRepository.containsUserById(id)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        if (!userRepository.containsUserById(friendId)) {
            throw new NotFoundException("Пользователь c id = " + friendId + " не найден");
        }
        userRepository.addFriend(id, friendId);
    }


    @Override
    public void deleteFriend(long id, long friendId) {
        if (!userRepository.containsUserById(id)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        if (!userRepository.containsUserById(friendId)) {
            throw new NotFoundException("Пользователь c id = " + friendId + " не найден");
        }
        userRepository.deleteFriend(id, friendId);
    }


    @Override
    public List<User> getFriendsList(long id) {
        if (!userRepository.containsUserById(id)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        return userRepository.getFriendsList(id);
    }


    @Override
    public List<User> getCommonFriendsList(long id, long otherId) {
        if (!userRepository.containsUserById(id)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        if (!userRepository.containsUserById(otherId)) {
            throw new NotFoundException("Пользователь c id = " + otherId + " не найден");
        }

        return userRepository.getCommonFriendsList(id, otherId);
    }

}
