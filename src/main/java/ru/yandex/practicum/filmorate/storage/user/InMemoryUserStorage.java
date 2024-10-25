package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    //Long - id юзера, у которого есть друзья
    // Set<Long> - сет id-шников его друзей
    private final HashMap<Long, Set<Long>> userFriendsIds = new HashMap<>();

    private long counterId = 0;

    private long getNextId() {
        return ++counterId;
    }

    @Override
    public List<User> getUserList() {
        log.info("Получен запрос на список всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        log.info("Получен запрос на добавление пользователя: {}", user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавление пользователя: {} - закончено, присвоен id: {}", user, user.getId());
        return user;
    }


    @Override
    public User updateUser(User newUser) {
        log.info("Получен запрос на обновление данных пользователя c id: {}", newUser.getId());
        User oldUser = users.get(newUser.getId());
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


    @Override
    public void addFriend(long id, long friendId) {
        log.info("Получен запрос на добавление в друзья пользователей c id: {} и {}", id, friendId);

        Set<Long> usFriendsIds = userFriendsIds.computeIfAbsent(id, userId -> new HashSet<>());
        if (usFriendsIds.contains(friendId)) {
            log.warn("Пользователь с id = {} уже добавил в друзья пользователя с id = {}", id, friendId);
            return;
        }
        usFriendsIds.add(friendId);
        Set<Long> frFriendsIds = userFriendsIds.computeIfAbsent(friendId, frId -> new HashSet<>());
        frFriendsIds.add(id);
        log.info("Пользователи c id: {} и {} добавлены друг к другу в друзья", id, friendId);
    }


    @Override
    public void deleteFriend(long id, long friendId) {
        log.info("Получен запрос на удаление из друзей пользователей c id: {} и {}", id, friendId);
        Set<Long> usFriendsIds = userFriendsIds.computeIfAbsent(id, userId -> new HashSet<>());
        usFriendsIds.remove(friendId);
        Set<Long> frFriendsIds = userFriendsIds.computeIfAbsent(friendId, frId -> new HashSet<>());
        frFriendsIds.remove(id);
        log.info("Пользователи c id: {} и {} удалены из друзей", id, friendId);
    }


    @Override
    public List<User> getFriendsList(long id) {
        log.info("Получен запрос на список друзей пользователя c id: {}", id);
        List<User> friendsList = new ArrayList<>();
        Set<Long> usFriendsIds = userFriendsIds.get(id);
        if (!(usFriendsIds == null)) {
            for (Long friendsId : usFriendsIds) {
                friendsList.add(users.get(friendsId));
            }
        }
        log.info("Список друзей пользователя c id: {} готов к отправке", id);
        return friendsList;
    }


    @Override
    public List<User> getCommonFriendsList(long id, long otherId) {
        log.info("Получен запрос на список общих друзей пользователей c id: {} и {}", id, otherId);

        Set<Long> usFriendsIds = userFriendsIds.get(id);
        Set<Long> otFriendsIds = userFriendsIds.get(otherId);
        List<User> commonFriendsList = new ArrayList<>();

        if (usFriendsIds != null && otFriendsIds != null) {
            Set<Long> commonFriendsIds = new HashSet<>(usFriendsIds);
            commonFriendsIds.retainAll(otFriendsIds);
            for (Long friendsId : commonFriendsIds) {
                commonFriendsList.add(users.get(friendsId));
            }
        }
        log.info("Список общих друзей пользователей c id: {} и {} готов к отправке", id, otherId);
        return commonFriendsList;
    }

    @Override
    public boolean containsUserById(long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean existByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

}
