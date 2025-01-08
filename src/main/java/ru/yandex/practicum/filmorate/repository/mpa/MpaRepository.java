package ru.yandex.practicum.filmorate.repository.mpa;

import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.util.List;

public interface MpaRepository {

    List<RatingMPA> getAllMpa();

    RatingMPA getMpaById(int id);
}
