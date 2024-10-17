package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> getFilmsList();

    Film createFilm(Film film);

    Film updateFilm(Film newFilm);

    void addLike(long id, long userId);

    void deleteLike(long id, long userId);

    List<Film> getPopularFilms(int count);
}
