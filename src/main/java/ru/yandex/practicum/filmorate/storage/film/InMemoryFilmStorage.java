package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    //Long - id фильма, у которого есть лайки
    // Set<Long> - сет id-шников пользователей, которые поставили лайки этому фильму
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private long counterId = 0;

    private long getNextId() {
        return ++counterId;
    }

    private final InMemoryUserStorage inMemoryUserStorage;

    @Autowired
    public InMemoryFilmStorage(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    @Override
    public List<Film> getFilmsList() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film createFilm(Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        if (films.containsValue(film)) { // сравнивает только name и releaseDate
            throw new DuplicatedDataException("Фильм с таким названием и годом выпуска уже есть в списке");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавление фильма: {} - закончено, присвоен id: {}", film, film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        log.info("Получен запрос на обновление фильма: {}", newFilm);
        Long newFilmId = newFilm.getId();
        if (newFilmId == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilmId)) {
            throw new NotFoundException("Фильм с id = " + newFilmId + " не найден");
        }
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

    @Override
    public void addLike(long id, long userId) {
        log.info("Получен запрос поставить лайк от пользователя с id = {} фильму с id = {}", userId, id);
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм c id = " + id + " не найден");
        }
        if (!inMemoryUserStorage.containsUser(userId)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }

        Set<Long> filmLikes = likes.computeIfAbsent(id, filmId -> new HashSet<>());

        if (filmLikes.contains(userId)) {
            log.warn("Пользователь с id = {} уже ставил лайк фильму с id = {}", userId, id);
            return;
        }
        filmLikes.add(userId);
        log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, id);
    }

    @Override
    public void deleteLike(long id, long userId) {
        log.info("Получен запрос убрать лайк от пользователя с id = {} фильму с id = {}", userId, id);
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм c id = " + id + " не найден");
        }
        if (!inMemoryUserStorage.containsUser(userId)) {
            throw new NotFoundException("Пользователь c id = " + id + " не найден");
        }

        Set<Long> filmLikes = likes.computeIfAbsent(id, filmId -> new HashSet<>());

        filmLikes.remove(userId);
        log.info("Пользователь с id = {} убрал лайк фильму с id = {}", userId, id);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        log.info("Получен запрос на получение наиболее популярных фильмов");
        return likes.entrySet().stream()
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()))
                .limit(count)
                .map(entry -> films.get(entry.getKey()))
                .collect(Collectors.toList());
    }
}
