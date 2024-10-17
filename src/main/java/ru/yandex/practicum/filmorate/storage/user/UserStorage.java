package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    List<User> getUserList();

    User createUser(User user);

    User updateUser(User newUser);

    void addFriend(long id, long friendId);

    void deleteFriend(long id, long friendId);

    List<User> getFriendsList(long id);

    List<User> getCommonFriendsList(long id, long otherId);
}
