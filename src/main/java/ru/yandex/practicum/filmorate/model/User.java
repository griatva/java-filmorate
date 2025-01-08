package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class User {

    private Long id;

    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Некорректный адрес электронной почты")
    private String email;

    @NotNull(message = "Это поле обязательно для заполнения")
    @Pattern(regexp = "^[^\\s]+$", message = "Логин не может быть пустым, состоять только из пробелов или содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Это поле обязательно для заполнения")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Long> friendsIds;
}