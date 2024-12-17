package ru.yandex.practicum.filmorate.repository.film;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Repository
public class JdbcFilmRepository implements FilmRepository {

    private JdbcTemplate jdbc;

    @Override
    public Optional<Film> findFilmById(long id) {
        log.info("Получен запрос на поиск фильма по его id: {}", id);

        final String FIND_BY_ID = "SELECT f.film_id,\n" +
                "f.name AS film_name,\n" +
                "f.description AS film_description,\n" +
                "f.release_date AS release_date,\n" +
                "f.duration AS film_duration,\n" +
                "rm.rating_mpa_id AS id_rating_mpa,\n" +
                "rm.name AS name_rating_mpa,\n" +
                "(SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) AS likes_count,\n" +
                "g.genre_id AS id_genre,\n" +
                "g.name AS name_genre\n" +
                "FROM films AS f\n" +
                "LEFT JOIN rating_mpa AS rm ON f.rating_mpa_id = rm.rating_mpa_id\n" +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id\n" +
                "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id\n" +
                "WHERE f.film_id = ?;";

        return jdbc.query(FIND_BY_ID, new ResultSetExtractor<Optional<Film>>() {
            @Override
            public Optional<Film> extractData(ResultSet rs) throws SQLException {
                Film film = null;
                while (rs.next()) {
                    if (film == null) {
                        film = extractFilm(rs);
                    }
                    addGenreToFilm(rs, film);
                }
                log.info("Подготовлен Optional, который, возможно, содержит фильм с id = {}", id);
                return Optional.ofNullable(film);
            }
        }, id);
    }

    @Override
    public List<Film> getFilmsList() {
        log.info("Получен запрос на получение списка всех фильмов");

        final String GET_ALL = "SELECT \n" +
                "    f.film_id AS id_film,\n" +
                "    f.name AS film_name,\n" +
                "    f.description AS film_description,\n" +
                "    f.release_date AS release_date,\n" +
                "    f.duration AS film_duration,\n" +
                "    rm.rating_mpa_id AS id_rating_mpa,\n" +
                "    rm.name AS name_rating_mpa,\n" +
                "    (\n" +
                "        SELECT COUNT(*) \n" +
                "        FROM likes l \n" +
                "        WHERE l.film_id = f.film_id\n" +
                "    ) AS likes_count,\n" +
                "    fg.genre_id AS id_genre,\n" +
                "    g.name AS name_genre\n" +
                "FROM films AS f\n" +
                "LEFT JOIN rating_mpa AS rm ON f.rating_mpa_id = rm.rating_mpa_id\n" +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id\n" +
                "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id\n" +
                "ORDER BY id_film;";

        return jdbc.query(GET_ALL, new ResultSetExtractor<List<Film>>() {
            @Override
            public List<Film> extractData(ResultSet rs) throws SQLException {
                Map<Long, Film> films = new LinkedHashMap<>();

                while (rs.next()) {
                    long filmId = rs.getLong("id_film");
                    Film film = films.get(filmId);
                    if (film == null) {
                        film = extractFilm(rs);
                        films.put(filmId, film);
                    }
                    addGenreToFilm(rs, film);
                }
                log.info("Список всех фильмов подготовлен");
                return new ArrayList<>(films.values());
            }
        });
    }

    @Override
    public Film createFilm(Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);

        final String CREATE_FILM = "INSERT INTO films (name, description, release_date, duration, rating_mpa_id) " +
                "VALUES (?, ?, ?, ?, ?);";
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_FILM, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        final Long generatedId = (Long) keyHolder.getKeys().get("film_id");
        if (generatedId != null) {
            film.setId(generatedId);
        } else {
            throw new RuntimeException("Не удалось получить сгенерированный ID для фильма");
        }

        insertGenres(film);

        log.info("Добавление фильма: {} - закончено, присвоен id: {}", film, film.getId());
        return film;
    }


    @Override
    public Film updateFilm(Film newFilm) {
        log.info("Получен запрос на обновление фильма: {}", newFilm);

        final String UPDATE_FILM = "update films set " +
                "name = ?, description = ?, release_date = ?, duration = ?, rating_mpa_id = ? " +
                "where film_id = ?;";
        long newFilmId = newFilm.getId();
        Integer ratingMPAId = (newFilm.getMpa() != null && newFilm.getMpa().getId() != null)
                ? newFilm.getMpa().getId()
                : null;
        jdbc.update(UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                ratingMPAId,
                newFilmId
        );

        final String DELETE_ALL_OLD_GENRES = "delete from film_genres \n" +
                "where film_id = ?;";
        jdbc.update(DELETE_ALL_OLD_GENRES, newFilmId);

        insertGenres(newFilm);

        final Film updatedFilm = findFilmById(newFilmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + newFilmId + " не найден"));

        log.info("Обновление фильма: {} - закончено.", newFilm);
        return updatedFilm;
    }


    private void insertGenres(Film film) {
        log.info("Начало добавления жанров фильма с id = {} в таблицу film_genres.", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            final String INSERT_GENRE = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = new ArrayList<>();
            for (Genre genre : film.getGenres()) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }
            jdbc.batchUpdate(INSERT_GENRE, batchArgs);
        }
        log.info("Конец добавления жанров фильма с id = {} в таблицу film_genres.", film.getId());

    }

    @Override
    public void addLike(long filmId, long userId) {
        log.info("Получен запрос поставить лайк от пользователя с id = {} фильму с id = {}", userId, filmId);

        final String INSERT_LIKE = "MERGE INTO likes (user_id, film_id) VALUES (?, ?);";
        int rowsAffected = jdbc.update(INSERT_LIKE, userId, filmId);

        if (rowsAffected > 0) {
            log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, filmId);
        } else {
            log.warn("Не удалось добавить like для userId: {} и filmId: {}", userId, filmId);
        }
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        log.info("Получен запрос удалить лайк от пользователя с id = {} фильму с id = {}", userId, filmId);
        final String DELETE_LIKE = "delete from likes \n" +
                "where (user_id, film_id)  = (?,?);";
        int rowsAffected = jdbc.update(DELETE_LIKE, userId, filmId);
        if (rowsAffected > 0) {
            log.info("Пользователь с id = {} удалил лайк фильму с id = {}", userId, filmId);
        } else {
            log.warn("Пользователю с id = {} не удалось удалить лайк фильму с id = {}", userId, filmId);
        }
    }

    public boolean isLikeExist(long filmId, long userId) {
        final String GET_LIKE = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer likeCount = jdbc.queryForObject(GET_LIKE, Integer.class, filmId, userId);
        return likeCount != null && likeCount > 0;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        log.info("Получен запрос на получение наиболее популярных фильмов");

        final String GET_POPULAR_FILMS =
                "SELECT " +
                        "    f.film_id AS id_film, " +
                        "    f.name AS film_name, " +
                        "    f.description AS film_description, " +
                        "    f.release_date AS release_date, " +
                        "    f.duration AS film_duration, " +
                        "    rm.rating_mpa_id AS id_rating_mpa, " +
                        "    rm.name AS name_rating_mpa, " +
                        "    (" +
                        "        SELECT COUNT(*) " +
                        "        FROM likes l " +
                        "        WHERE l.film_id = f.film_id " +
                        "    ) AS likes_count " +
                        "FROM films AS f " +
                        "LEFT JOIN rating_mpa AS rm ON f.rating_mpa_id = rm.rating_mpa_id " +
                        "ORDER BY likes_count DESC " +
                        "LIMIT ?";

        Map<Long, Film> films = jdbc.query(GET_POPULAR_FILMS, new Object[]{count}, (rs) -> {
            Map<Long, Film> result = new LinkedHashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("id_film");
                Film film = extractFilm(rs);
                result.put(filmId, film);
            }
            return result;
        });

        if (films == null || films.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> filmIds = new ArrayList<>(films.keySet());

        String filmIdsPlaceholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        final String GET_GENRES_FOR_FILMS =
                "SELECT fg.film_id, g.genre_id AS id_genre, g.name AS name_genre " +
                        "FROM film_genres fg " +
                        "JOIN genres g ON fg.genre_id = g.genre_id " +
                        "WHERE fg.film_id IN (" + filmIdsPlaceholders + ")";

        jdbc.query(GET_GENRES_FOR_FILMS, filmIds.toArray(), (rs) -> {
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Film filmForGenresAdding = films.get(filmId);
                if (films.containsKey(filmId)) {
                    addGenreToFilm(rs, filmForGenresAdding);
                }
            }
        });
        log.info("Список наиболее популярных фильмов подготовлен");
        return new ArrayList<>(films.values());
    }

    private Film extractFilm(ResultSet rs) {
        try {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("film_name"));
            film.setDescription(rs.getString("film_description"));
            film.setDuration(rs.getInt("film_duration"));
            film.setLikes(rs.getInt("likes_count"));

            Date releaseDate = rs.getDate("release_date");
            if (releaseDate != null) {
                film.setReleaseDate(releaseDate.toLocalDate());
            }

            int ratingId = rs.getInt("id_rating_mpa");
            String ratingName = rs.getString("name_rating_mpa");
            if (!rs.wasNull()) {
                RatingMPA rating = new RatingMPA(ratingId, ratingName);
                film.setMpa(rating);
            }
            film.setGenres(new LinkedHashSet<>());

            return film;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при извлечении фильма из ResultSet", e);
        }
    }

    private void addGenreToFilm(ResultSet rs, Film film) {
        try {
            int genreId = rs.getInt("id_genre");
            String genreName = rs.getString("name_genre");
            if (!rs.wasNull()) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(genreName);
                film.getGenres().add(genre);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении жанра к фильму", e);
        }
    }
}