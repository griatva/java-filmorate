package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.annotation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;


@Data
public class Film {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return Objects.equals(id, film.id) &&
                Objects.equals(name, film.name) &&
                Objects.equals(description, film.description) &&
                Objects.equals(releaseDate, film.releaseDate) &&
                Objects.equals(duration, film.duration) &&
                Objects.equals(genres, film.genres) &&
                Objects.equals(mpa, film.mpa) &&
                Objects.equals(likes, film.likes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, releaseDate, duration, genres, mpa, likes);
    }

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Длина описания должна быть не более 200 символов")
    private String description;

    @NotNull(message = "Это поле обязательно для заполнения")
    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;

    private LinkedHashSet<Genre> genres;

    private RatingMPA mpa;

    private Integer likes;
}