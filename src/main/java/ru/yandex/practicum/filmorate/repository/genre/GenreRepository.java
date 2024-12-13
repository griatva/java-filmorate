package ru.yandex.practicum.filmorate.repository.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreRepository {
    List<Genre> getByIds(List<Integer> ids);

    List<Genre> getAllGenres();

    Genre getGenreById(Integer id);
}
