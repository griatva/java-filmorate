package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private long counterId = 0;

    private long getNextId() {
        return ++counterId;
    }

    @GetMapping
    public List<Film> getFilmsList() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        for (Film value : films.values()) { // сравнивает только name и releaseDate
            if (value.equals(film)) {
                throw new DuplicatedDataException("Фильм с таким названием и годом выпуска уже есть в списке");
            }

        }
        film.setId(getNextId());
        films.put(film.getId(), film);


        log.info("Добавление фильма: {} - закончено, присвоен id: {}", film, film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма: {}", newFilm);
        Long newFilmId = newFilm.getId();
        if (newFilmId == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (!films.containsKey(newFilmId)) {
            throw new NotFoundException("Фильм с id = " + newFilmId + " не найден");
        } else {
            for (Film value : films.values()) {
                if (value.equals(newFilm) && !value.getId().equals(newFilmId)) {
                    throw new DuplicatedDataException("Фильм с таким названием и годом выпуска уже есть в списке");
                }
            }
            Film oldFilm = films.get(newFilmId);
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Обновление фильма: {} - закончено", oldFilm);
            return oldFilm;
        }
    }
}
