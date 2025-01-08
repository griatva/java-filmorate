package ru.yandex.practicum.filmorate.service.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.repository.mpa.MpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaServiceImpl implements MpaService {

    private final MpaRepository mpaRepository;

    @Override
    public List<RatingMPA> getAllMpa() {
        return mpaRepository.getAllMpa();
    }

    @Override
    public RatingMPA getMpaById(Integer id) {
        RatingMPA ratingMPA = mpaRepository.getMpaById(id);
        if (ratingMPA == null) {
            throw new NotFoundException("Рейтинг MPA с id = " + id + " отсутствует");
        }
        return ratingMPA;
    }
}
