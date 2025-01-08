package ru.yandex.practicum.filmorate.repository.genre;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Repository
public class JdbcGenreRepository implements GenreRepository {
    private JdbcTemplate jdbc;

    @Override
    public List<Genre> getByIds(List<Integer> ids) {
        log.info("Начало подготовки списка жанров по их id");
        if (ids == null || ids.isEmpty()) {
            log.info("Список id пуст или равен null. Возвращён пустой список.");
            return Collections.emptyList();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        final String GET_GENRES_BY_IDS = "select genre_id, name from genres where genre_id IN (" + placeholders + ")";

        List<Genre> genres = jdbc.query(GET_GENRES_BY_IDS, ids.toArray(), (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        });

        log.info("Завершение подготовки списка жанров.");
        return genres;
    }

    @Override
    public List<Genre> getAllGenres() {
        log.info("Получен запрос на получение списка всех жанров");

        final String GET_ALL_GENRES = "select * from genres;";

        List<Genre> genres = jdbc.query(GET_ALL_GENRES, new ResultSetExtractor<List<Genre>>() {
            @Override
            public List<Genre> extractData(ResultSet rs) throws SQLException {
                Map<Integer, Genre> genres = new LinkedHashMap<>();

                while (rs.next()) {
                    int genreId = rs.getInt("genre_id");
                    Genre genre = genres.get(genreId);

                    if (genre == null) {
                        genre = new Genre();
                        genre.setId(genreId);
                        genre.setName(rs.getString("name"));

                        genres.put(genreId, genre);
                    }
                }
                return new ArrayList<>(genres.values());
            }
        });

        log.info("Завершение получения списка жанров.");
        return genres;
    }

    @Override
    public Genre getGenreById(Integer id) {
        log.info("Получен запрос на получение жанра с id = {}", id);

        final String FIND_GENRE_BY_ID = "SELECT * FROM genres WHERE genre_id = ?;";

        try {
            Genre genre = jdbc.queryForObject(FIND_GENRE_BY_ID, new RowMapper<Genre>() {
                @Override
                public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("genre_id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                }
            }, id);

            log.info("Жанр с id = {} успешно найден: {}", id, genre);
            return genre;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
    }
}
