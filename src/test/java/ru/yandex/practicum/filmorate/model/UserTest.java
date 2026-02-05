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

@DisplayName("User model validation tests")
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
    @DisplayName("Should throw an error if the email field is null")
    void validateNullEmail() {
        User user = new User();
        user.setLogin("Lubov");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setEmail(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Email must not be blank", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the email field is empty")
    void validateEmptyEmail() {
        User user = new User();
        user.setLogin("Lubov");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Email must not be blank", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw two errors if the email field contains only spaces")
    void validateEmailConsistsOfSpaces() {
        User user = new User();
        user.setLogin("Lubov");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setEmail("  ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(2, violations.size(), "Incorrect number of validation errors");
    }

    @Test
    @DisplayName("Should throw an error if the email format is invalid")
    void validateCorrectEmail() {
        User user = new User();
        user.setLogin("Lubov");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setEmail("lubov.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Invalid email address", violation.getMessage());
    }


    @Test
    @DisplayName("Should throw an error if the login field is null")
    void validateNullLogin() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setLogin(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("This field is required", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the login field is empty")
    void validateEmptyLogin() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Login must not be blank, consist only of whitespace, or contain spaces",
                violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the login field contains only spaces")
    void validateLoginConsistsOfSpaces() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setLogin(" ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Login must not be blank, consist only of whitespace, or contain spaces",
                violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the login field contains spaces")
    void validateLoginContainsSpaces() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setBirthday(LocalDate.of(1910, 10, 10));

        user.setLogin("lubov E");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Login must not be blank, consist only of whitespace, or contain spaces",
                violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the birthday field is null")
    void validateNullBirthday() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setLogin("lubov");

        user.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("birthday", violation.getPropertyPath().toString());
        assertEquals("This field is required", violation.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the birthday is set in the future")
    void validateBirthdayInFuture() {
        User user = new User();
        user.setEmail("lubov@mail.ru");
        user.setLogin("lubov");

        user.setBirthday(LocalDate.of(3015, 10, 10));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "No validation error was thrown");
        assertEquals(1, violations.size(), "Incorrect number of validation errors");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("birthday", violation.getPropertyPath().toString());
        assertEquals("Birth date must not be in the future", violation.getMessage());
    }
}