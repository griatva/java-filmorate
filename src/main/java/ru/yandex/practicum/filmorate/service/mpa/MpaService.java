package ru.yandex.practicum.filmorate.service.mpa;

import ru.yandex.practicum.filmorate.model.RatingMPA;
import java.util.List;

public interface MpaService {

    List<RatingMPA> getAllMpa();
    RatingMPA getMpaById(Integer id);
}
