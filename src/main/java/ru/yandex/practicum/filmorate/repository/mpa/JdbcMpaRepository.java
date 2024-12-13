package ru.yandex.practicum.filmorate.repository.mpa;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Repository
public class JdbcMpaRepository implements MpaRepository{

    private JdbcTemplate jdbc;

    @Override
    public List<RatingMPA> getAllMpa() {
        log.info("Получен запрос на получение списка всех рейтингов MPA");

        final String GET_ALL_MPA = "SELECT * FROM rating_mpa;";

        List<RatingMPA> mpaList = jdbc.query(GET_ALL_MPA, new ResultSetExtractor<List<RatingMPA>>() {
            @Override
            public List<RatingMPA> extractData(ResultSet rs) throws SQLException {
                Map<Integer, RatingMPA> mpas = new LinkedHashMap<>();

                while (rs.next()) {
                    int mpaId = rs.getInt("rating_mpa_id");
                    RatingMPA ratingMPA = mpas.get(mpaId);

                    if (ratingMPA == null) {
                        ratingMPA = new RatingMPA(mpaId, rs.getString("name"));
                        mpas.put(mpaId, ratingMPA);
                    }
                }
                return new ArrayList<>(mpas.values());
            }
        });

        log.info("Список рейтингов MPA успешно получен");
        return mpaList;
    }

    @Override
    public RatingMPA getMpaById(int id) {
        log.info("Получен запрос на получение рейтинга MPA с id = {}", id);

        final String FIND_MPA_BY_ID = "SELECT * FROM rating_mpa WHERE rating_mpa_id = ?;";

        try {
            RatingMPA ratingMPA = jdbc.queryForObject(FIND_MPA_BY_ID, new RowMapper<RatingMPA>() {
                @Override
                public RatingMPA mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new RatingMPA(rs.getInt("rating_mpa_id"), rs.getString("name"));
                }
            }, id);

            log.info("Рейтинг MPA с id = {} найден: {}", id, ratingMPA);
            return ratingMPA;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
