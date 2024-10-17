package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public List<User> getUserList() {
        return userStorage.getUserList();
    }

    @Override
    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User newUser) {
        return userStorage.updateUser(newUser);
    }

    @Override
    public void addFriend(long id, long friendId) {
        userStorage.addFriend(id, friendId);
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    @Override
    public List<User> getFriendsList(long id) {
        return userStorage.getFriendsList(id);
    }

    @Override
    public List<User> getCommonFriendsList(long id, long otherId) {
        return userStorage.getCommonFriendsList(id, otherId);
    }

}
