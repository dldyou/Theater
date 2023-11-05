package repository;

import database.StringIdEntityDatabase;
import entity.Movie;
import entity.MovieSchedule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

public class MovieScheduleRepository extends StringIdEntityDatabase<MovieSchedule> {

    public MovieScheduleRepository(String path) {
        super(MovieSchedule.class, 6, path);
    }

    public List<LocalTime> findAllMoviesStartAtTimeByDateAndRoomNumber(String movieId, LocalDate localDate,
                                                                       Long roomNumber) {
        return findAll().stream()
                .filter(movieSchedule -> Objects.equals(movieSchedule.getMovie().getId(), movieId))
                .filter(movieSchedule -> Objects.equals(movieSchedule.getStartAtDate(), localDate))
                .filter(movieSchedule -> Objects.equals(Long.parseLong(movieSchedule.getRoom().getRoomNumber()),
                        roomNumber))
                .map(MovieSchedule::getStartAtTime)
                .sorted()
                .collect(Collectors.toList());
    }

    public MovieSchedule findOne(Movie movie, LocalDate date, Long roomNumber, LocalTime startAt) {
        return findAll().stream()
                .filter(movieSchedule -> Objects.equals(movieSchedule.getMovie().getId(), movie.getId()))
                .filter(movieSchedule -> Objects.equals(movieSchedule.getStartAtDate(), date))
                .filter(movieSchedule -> Objects.equals(movieSchedule.getRoom().getRoomNumber(), String.valueOf(roomNumber)))
                .filter(movieSchedule -> Objects.equals(movieSchedule.getStartAtTime(), startAt))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
