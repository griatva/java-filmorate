package ru.yandex.practicum.filmorate.repository.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Repository
public class JdbcUserRepository implements UserRepository {

    private JdbcTemplate jdbc;

    @Override
    public List<User> getUserList() {
        log.info("Получен запрос на список всех пользователей");
        final String GET_ALL_USERS = "SELECT u.*, \n" +
                "       f.friend_id\n" +
                "FROM users AS u\n" +
                "LEFT JOIN friendship AS f \n" +
                "  ON u.user_id = f.user_id AND f.status_id = 1;";

        return getUsersList(GET_ALL_USERS);
    }

    private List<User> getUsersList(String sqlQuery, Object... params){
        log.info("Начало подготовки списка пользователей");
        return jdbc.query(sqlQuery, params, new ResultSetExtractor<List<User>>() {
            @Override
            public List<User> extractData(ResultSet rs) throws SQLException {
                Map<Long, User> users = new LinkedHashMap<>();

                while (rs.next()) {
                    long userId = rs.getLong("user_id");
                    User user = users.get(userId);

                    if (user == null) {
                        user = new User();
                        user.setId(userId);
                        user.setLogin(rs.getString("login"));
                        user.setName(rs.getString("name"));
                        user.setEmail(rs.getString("email"));
                        user.setBirthday(rs.getDate("birthday").toLocalDate());
                        user.setFriendsIds(new LinkedHashSet<>());

                        users.put(userId, user);
                    }

                    Long friendId = rs.getObject("friend_id") != null ? rs.getLong("friend_id") : null;
                    if (friendId != null && friendId != userId) {
                        user.getFriendsIds().add(friendId);
                    }
                }
                log.info("Список подготовлен");
                return new ArrayList<>(users.values());
            }
        });
    }

    @Override
    public User createUser(User user) {
        log.info("Получен запрос на добавление пользователя");
        final String CREATE_USER = "INSERT INTO users (login, name, email, birthday) " +
                "VALUES (?, ?, ?, ?);";
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getLogin());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        final Long generatedId = (Long) keyHolder.getKeys().get("user_id");
        if (generatedId != null) {
            user.setId(generatedId);
        } else {
            throw new RuntimeException("Не удалось получить сгенерированный ID для пользователя");
        }
        log.info("Добавление пользователя: {} - закончено, присвоен id: {}", user, user.getId());
        return user;
    }


    @Override
    public User updateUser(User newUser) {
        log.info("Получен запрос на обновление данных пользователя c id: {}", newUser.getId());

        final String UPDATE_USER = "update users set " +
                "login = ?, name = ?, email = ?, birthday = ? " +
                "where user_id = ?;";
        long newUserId = newUser.getId();

        jdbc.update(UPDATE_USER
                , newUser.getLogin()
                , newUser.getName()
                , newUser.getEmail()
                , newUser.getBirthday()
                , newUserId
        );

        final User updatedUser = getUserById(newUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + newUserId + " не найден"));

        log.info("Обновление пользователя: {} - закончено.", newUser);
        return updatedUser;
    }

    @Override
    public void addFriend(long userId, long friendId) {

        if (userId == friendId) {
            log.warn("Попытка добавить самого себя в друзья: userId = {}", userId);
            return;
        }

        log.info("Запрос на добавление друга: userId = {}, friendId = {}", userId, friendId);

        boolean directPairExists = pairExists(userId, friendId);
        if (directPairExists) {
            log.info("Прямая дружба уже существует: пользователь {} уже в друзьях у {}", friendId, userId);
            return;
        }

        boolean reversePairExists = pairExists(friendId, userId);
        if (reversePairExists) {
            log.info("Обратная дружба найдена: подтверждение дружбы между {} и {}", friendId, userId);

            final String INSERT_DIRECT_PAIR =
                    "MERGE INTO friendship (user_id, friend_id, status_id)\n" +
                            "    KEY(user_id, friend_id)\n" +
                            "    VALUES (?, ?, ?)";
            jdbc.update(INSERT_DIRECT_PAIR, userId, friendId, 1);

            final String UPDATE_STATUSES =
                    "UPDATE friendship SET status_id = 1 WHERE (user_id = ? AND friend_id = ?) " +
                            "OR (user_id = ? AND friend_id = ?)";
            jdbc.update(UPDATE_STATUSES, userId, friendId, friendId, userId);

            log.info("Дружба подтверждена: пользователи {} и {} теперь друзья", userId, friendId);
        } else {
            final String INSERT_FRIENDSHIP =
                    "INSERT INTO friendship (user_id, friend_id, status_id) VALUES (?, ?, 2)";
            jdbc.update(INSERT_FRIENDSHIP, userId, friendId);

            log.info("Запрос в друзья: пользователь {} отправил запрос пользователю {} со статусом 2 (Pending)",
                    userId, friendId);
        }
    }

    private boolean pairExists(long userId, long friendId) {
        log.info("Начало поиска пары в БД");
        final String CHECK_PAIR =
                "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbc.queryForObject(CHECK_PAIR, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        log.info("Запрос на удаление друга: userId = {}, friendId = {}", userId, friendId);

        if (!pairExists(userId, friendId)) {
            log.info("Пользователь {} не имеет в друзьях пользователя {}", userId, friendId);
            return;
        }

        final String DELETE_DIRECT_PAIR =
                "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbc.update(DELETE_DIRECT_PAIR, userId, friendId);
        log.info("Удалена запись дружбы: {} -> {}", userId, friendId);

        if (pairExists(friendId, userId)) {
            final String UPDATE_REVERSE_PAIR =
                    "UPDATE friendship SET status_id = 2 WHERE user_id = ? AND friend_id = ?";
            jdbc.update(UPDATE_REVERSE_PAIR, friendId, userId);
            log.info("Обновлена запись дружбы: {} -> {} теперь имеет статус 2 (Pending)", friendId, userId);
        } else {
            log.info("Односторонняя дружба удалена: пользователь {} больше не в друзьях у пользователя {}",
                    userId, friendId);
        }
    }

    @Override
    public List<User> getFriendsList(long id) {
        log.info("Получен запрос на получение списка друзей пользователя c id: {}", id);

        final String GET_ACCEPTED_FRIENDS = "SELECT \n" +
                "    u.user_id AS user_id,\n" +
                "    u.login AS login,\n" +
                "    u.name AS name,\n" +
                "    u.email AS email,\n" +
                "    u.birthday AS birthday,\n" +
                "    fof.user_id AS friend_id\n" +
                "FROM friendship AS f\n" +
                "JOIN users AS u ON f.friend_id = u.user_id\n" +
                "LEFT JOIN friendship AS f2 ON u.user_id = f2.user_id\n" +
                "LEFT JOIN users AS fof ON f2.friend_id = fof.user_id\n" +
                "WHERE f.user_id = ?;";

        return getUsersList(GET_ACCEPTED_FRIENDS, id);
    }

    @Override
    public List<User> getCommonFriendsList(long id, long otherId) {
        log.info("Запрос на получение списка общих друзей следующих пользователей: userId = {}, friendId = {}",
                id, otherId);

        final String GET_COMMON_FRIENDS = "SELECT u.user_id AS user_id,\n" +
                "       u.login AS login,\n" +
                "       u.name AS name,\n" +
                "       u.email AS email,\n" +
                "       u.birthday AS birthday,\n" +
                "       f1.friend_id AS friend_id\n" +
                "FROM users AS u\n" +
                "JOIN friendship AS f1 ON u.user_id = f1.friend_id\n" +
                "JOIN friendship AS f2 ON u.user_id = f2.friend_id\n" +
                "WHERE f1.user_id = ? AND f2.user_id = ?;";

        return getUsersList(GET_COMMON_FRIENDS, id, otherId);
    }

    @Override
    public boolean existByEmail(String email) {
        log.info("Проверка существования пользователя с email: {}", email);

        final String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbc.queryForObject(CHECK_EMAIL_EXISTS, Integer.class, email);
        boolean exists = count != null && count > 0;
        if (exists) {
            log.info("Пользователь с email {} найден в базе данных.", email);
        } else {
            log.info("Пользователь с email {} не найден.", email);
        }
        return exists;
    }

    @Override
    public boolean containsUserById(long id) {
        log.info("Проверка существования пользователя с id: {}", id);

        final String CHECK_USER_EXISTS_BY_ID = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbc.queryForObject(CHECK_USER_EXISTS_BY_ID, Integer.class, id);
        boolean exists = count != null && count > 0;
        if (exists) {
            log.info("Пользователь с id {} существует в базе данных.", id);
        } else {
            log.info("Пользователь с id {} не найден в базе данных.", id);
        }
        return exists;
    }

    private Optional<User> getUserById(Long id) {
        log.info("Поиск пользователя с id: {}", id);

        final String GET_USER_BY_ID = "SELECT u.user_id AS user_id,\n" +
                "       u.login AS login,\n" +
                "       u.name AS name,\n" +
                "       u.email AS email,\n" +
                "       u.birthday AS birthday,\n" +
                "       f.friend_id AS friend_id" +
                "        FROM users AS u\n" +
                "        LEFT JOIN friendship AS f \n" +
                "        ON u.user_id = f.user_id AND f.status_id = 1\n" +
                "        WHERE u.user_id = ?;";

        User user = jdbc.queryForObject(GET_USER_BY_ID, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User user = new User();
                user.setId(rs.getLong("user_id"));
                user.setLogin(rs.getString("login"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setBirthday(rs.getDate("birthday").toLocalDate());
                user.setFriendsIds(new LinkedHashSet<>());

                do {
                    user.getFriendsIds().add(rs.getLong("friend_id"));
                } while (rs.next());
                return user;
            }
        }, id);
        return Optional.ofNullable(user);
    }
}
