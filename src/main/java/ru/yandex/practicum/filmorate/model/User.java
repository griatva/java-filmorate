package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class User {

    private Long id;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "This field is required")
    @Pattern(regexp = "^[^\\s]+$", message = "Login must not be blank, consist only of whitespace, or contain spaces")
    private String login;

    private String name;

    @NotNull(message = "This field is required")
    @PastOrPresent(message = "Birth date must not be in the future")
    private LocalDate birthday;

    private Set<Long> friendsIds;
}