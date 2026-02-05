package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testing Film model validation")
class FilmTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void init() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    @AfterAll
    static void close() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    @DisplayName("Should throw an error if the name field is null")
    void validateNullName() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.now());

        film.setName(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Name must not be blank", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the name field is empty")
    void validateEmptyName() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.now());

        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "No validation error was thrown");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Name must not be blank", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the name field is empty")
    void validateNameConsistsOfSpaces() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.now());

        film.setName("  ");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "No validation error was thrown");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Name must not be blank", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the description exceeds 200 characters")
    void validateDescriptionLength() {
        Film film = new Film();
        film.setName("Name");
        film.setReleaseDate(LocalDate.now());

        film.setDescription("This film tells the story of frightening monsters who scare children at night to generate " +
                "energy for their world. Everything changes when a curious child accidentally enters the monster world, " +
                "causing chaos, fear, and unexpected consequences for both sides."
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "No validation error was thrown");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("description", violation.getPropertyPath().toString());
        assertEquals("Description must not exceed 200 characters", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the release date is null")
    void validateNullReleaseDate() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("A very funny film");

        film.setReleaseDate(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "No validation error was thrown");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("releaseDate", violation.getPropertyPath().toString());
        assertEquals("This field is required", violation.getMessage());
    }


    @Test
    @DisplayName("Should throw an error if the release date is before December 28, 1895")
    void validateReleaseDate() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("A very funny film");

        film.setReleaseDate(LocalDate.of(1894, Month.SEPTEMBER, 10));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "No validation error was thrown");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("releaseDate", violation.getPropertyPath().toString());
        assertEquals("The film must be released after December 28, 1895", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the duration is negative")
    void validatePositiveDuration() {
        Film film = new Film();
        film.setName("Name");
        film.setReleaseDate(LocalDate.now());

        film.setDuration(-500);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "No validation error was thrown");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("duration", violation.getPropertyPath().toString());
        assertEquals("Movie duration must be positive", violation.getMessage());
    }

}