package ru.yandex.practicum.filmorate.repository.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserRepository {

    List<User> getUserList();

    User createUser(User user);

    User updateUser(User newUser);

    void addFriend(long id, long friendId);

    void deleteFriend(long id, long friendId);

    List<User> getFriendsList(long id);

    List<User> getCommonFriendsList(long id, long otherId);

    boolean existByEmail(String email);

    boolean containsUserById(long id);
}
