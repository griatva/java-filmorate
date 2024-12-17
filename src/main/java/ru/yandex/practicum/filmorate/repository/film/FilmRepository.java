package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmRepository {

    List<Film> getFilmsList();

    Film createFilm(Film film);

    Film updateFilm(Film newFilm);

    void addLike(long id, long userId);

    void deleteLike(long id, long userId);

    List<Film> getPopularFilms(int count);

    Optional<Film> findFilmById(long id);

    boolean isLikeExist(long filmId, long userId);

}
