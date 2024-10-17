package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmServiceImpl(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @Override
    public List<Film> getFilmsList() {
        return filmStorage.getFilmsList();
    }

    @Override
    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    @Override
    public Film updateFilm(Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    @Override
    public void addLike(long id, long userId) {
        filmStorage.addLike(id, userId);
    }

    @Override
    public void deleteLike(long id, long userId) {
        filmStorage.deleteLike(id, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}
