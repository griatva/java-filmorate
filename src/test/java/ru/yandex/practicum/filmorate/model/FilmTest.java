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

@DisplayName("Тестирование валидации модели Film")
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
    @DisplayName("Должен выкинуть ошибку, если поле name равно null")
    void validateNullName() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.now());

        film.setName(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Название не может быть пустым", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если поле name пустое")
    void validateEmptyName() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.now());

        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Ошибка не появилась");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Название не может быть пустым", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если поле name пустое")
    void validateNameConsistsOfSpaces() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.now());

        film.setName("  ");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Ошибка не появилась");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Название не может быть пустым", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если описание содержит более 200 символов")
    void validateDescriptionLength() {
        Film film = new Film();
        film.setName("Name");
        film.setReleaseDate(LocalDate.now());

        film.setDescription("Этот фильм про страшных монстров, которые пугают детей по ночам для получения электроэнергии для своего мира. " +
                "История начинается с момента, когда все пошло не по плану и ребенок проникает в мир монстров.");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Ошибка не появилась");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("description", violation.getPropertyPath().toString());
        assertEquals("Длина описания должна быть не более 200 символов", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если дата выпуска null")
    void validateNullReleaseDate() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Очень веселый фильм");

        film.setReleaseDate(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Ошибка не появилась");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("releaseDate", violation.getPropertyPath().toString());
        assertEquals("Это поле обязательно для заполнения", violation.getMessage());
    }


    @Test
    @DisplayName("Должен выкинуть ошибку, если дата выпуска раньше 28.12.1895")
    void validateReleaseDate() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Очень веселый фильм");

        film.setReleaseDate(LocalDate.of(1894, Month.SEPTEMBER, 10));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Ошибка не появилась");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("releaseDate", violation.getPropertyPath().toString());
        assertEquals("Фильм должен быть выпущен позже 28 декабря 1895 года", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если длительность отрицательная")
    void validatePositiveDuration() {
        Film film = new Film();
        film.setName("Name");
        film.setReleaseDate(LocalDate.now());

        film.setDuration(-500);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Ошибка не появилась");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("duration", violation.getPropertyPath().toString());
        assertEquals("Продолжительность фильма должна быть положительной", violation.getMessage());
    }

}