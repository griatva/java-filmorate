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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование валидации модели User")
class UserTest {


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
    @DisplayName("Должен выкинуть ошибку, если поле email равно null")
    void validateNullEmail() {
        User user = new User();
        user.setLogin("Lubov");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setEmail(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Адрес электронной почты не может быть пустым", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если поле email пустое")
    void validateEmptyEmail() {
        User user = new User();
        user.setLogin("Lubov");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Адрес электронной почты не может быть пустым", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть 2 ошибки, если поле email состоит из пробелов")
    void validateEmailConsistsOfSpaces() {
        User user = new User();
        user.setLogin("Lubov");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setEmail("  ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(2, violations.size(), "Неправильное количество ошибок");
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если email не соответствует формату")
    void validateCorrectEmail() {
        User user = new User();
        user.setLogin("Lubov");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setEmail("lubov.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Некорректный адрес электронной почты", violation.getMessage());
    }


    @Test
    @DisplayName("Должен выкинуть ошибку, если поле login равно null")
    void validateNullLogin() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setLogin(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Это поле обязательно для заполнения", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если поле login пустое")
    void validateEmptyLogin() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Логин не может быть пустым, состоять только из пробелов или содержать пробелы", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если поле login состоит из пробелов")
    void validateLoginConsistsOfSpaces() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setLogin(" ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Логин не может быть пустым, состоять только из пробелов или содержать пробелы",
                violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если поле login содержит пробелы")
    void validateLoginContainsSpaces() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setLogin("lubov E");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Логин не может быть пустым, состоять только из пробелов или содержать пробелы",
                violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если поле birthday равно null")
    void validateNullBirthday() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setLogin("lubov");

        user.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("birthday", violation.getPropertyPath().toString());
        assertEquals("Это поле обязательно для заполнения", violation.getMessage());
    }

    @Test
    @DisplayName("Должен выкинуть ошибку, если поле birthday указано в будущем")
    void validateBirthdayInFuture() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setLogin("lubov");

        user.setBirthday(LocalDate.of(3015, 10, 10));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Ошибка не появилась");
        assertEquals(1, violations.size(), "Неправильное количество ошибок");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("birthday", violation.getPropertyPath().toString());
        assertEquals("Дата рождения не может быть в будущем", violation.getMessage());
    }

}