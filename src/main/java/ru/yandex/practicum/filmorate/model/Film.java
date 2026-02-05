package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.annotation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.Set;


@Data
public class Film {

    private Long id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @NotNull(message = "This field is required")
    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Movie duration must be positive")
    private Integer duration;

    private Set<Genre> genres;

    private RatingMPA mpa;

    private Integer likes;
}