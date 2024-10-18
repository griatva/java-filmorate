package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;


    @Override
    public List<Film> getFilmsList() {
        return filmStorage.getFilmsList();
    }


    @Override
    public Film createFilm(Film film) {
        if (filmStorage.existByNameAndReleaseDate(film)) { // сравнивает только name и releaseDate
            throw new DuplicatedDataException("Фильм с таким названием и годом выпуска уже есть в списке");
        }
        return filmStorage.createFilm(film);
    }


    @Override
    public Film updateFilm(Film newFilm) {
        Long newFilmId = newFilm.getId();
        if (newFilmId == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!filmStorage.containsFilmById(newFilmId)) {
            throw new NotFoundException("Фильм с id = " + newFilmId + " не найден");
        }
        Optional<Film> sameFilm = filmStorage.findByNameAndReleaseDate(newFilm);
        if (sameFilm.isPresent() && !sameFilm.get().getId().equals(newFilmId)) {
            throw new DuplicatedDataException("Фильм с таким названием и годом выпуска уже есть в списке");
        }
        return filmStorage.updateFilm(newFilm);
    }


    @Override
    public void addLike(long id, long userId) {
        if (!filmStorage.containsFilmById(id)) {
            throw new NotFoundException("Фильм c id = " + id + " не найден");
        }
        if (!userStorage.containsUserById(userId)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        filmStorage.addLike(id, userId);
    }


    @Override
    public void deleteLike(long id, long userId) {
        if (!filmStorage.containsFilmById(id)) {
            throw new NotFoundException("Фильм c id = " + id + " не найден");
        }
        if (!userStorage.containsUserById(userId)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }
        filmStorage.deleteLike(id, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}
