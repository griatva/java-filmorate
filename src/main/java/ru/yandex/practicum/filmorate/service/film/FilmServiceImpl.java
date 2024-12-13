package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.genre.GenreRepository;
import ru.yandex.practicum.filmorate.repository.mpa.MpaRepository;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;


    @Override
    public List<Film> getFilmsList() {
        return filmRepository.getFilmsList();
    }


    @Override
    public Film createFilm(Film film) {

        checkForRelatedData(film);

        return filmRepository.createFilm(film);
    }



    @Override
    public Film updateFilm(Film newFilm) {
        final Long newFilmId = newFilm.getId();
        if (newFilmId == null) {
            throw new ValidationException("Id должен быть указан");
        }

        final Film existingFilm = filmRepository.findFilmById(newFilmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + newFilmId + " не найден"));

        checkForRelatedData(newFilm);

        return filmRepository.updateFilm(newFilm);
    }

    private void checkForRelatedData(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            RatingMPA ratingMPA = mpaRepository.getMpaById(film.getMpa().getId());
            if (ratingMPA == null) {
                throw new ValidationException("MPA с id " + film.getMpa().getId() + " не найден");
            }
            film.getMpa().setName(ratingMPA.getName());
        }

        if (film.getGenres() != null) {
            final List<Integer> newFilmsGenreIds = film.getGenres().stream().map(Genre::getId).toList();
            final List<Genre> genresInDB = genreRepository.getByIds(newFilmsGenreIds);
            if (newFilmsGenreIds.size() != genresInDB.size()) {
                throw new ValidationException("Жанры не найдены");
            }
        }
    }


    @Override
    public void addLike(long filmId, long userId) {
        Film existingFilm = filmRepository.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));

        if (!userRepository.containsUserById(userId)) {
            throw new NotFoundException("Пользователь c id = " + userId + " не найден");
        }
        filmRepository.addLike(filmId, userId);
    }


    @Override
    public void deleteLike(long filmId, long userId) {
        Film existingFilm = filmRepository.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));

        if (!userRepository.containsUserById(userId)) {
            throw new NotFoundException("Пользователь c id = " + filmId + " не найден");
        }
        filmRepository.deleteLike(filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return filmRepository.getPopularFilms(count);
    }

    @Override
    public Film findFilmById(long id) {
        return filmRepository.findFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }
}
