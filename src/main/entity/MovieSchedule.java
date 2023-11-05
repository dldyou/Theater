package entity;

import database.DatabaseContext;
import exception.EntityInstantiateException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MovieSchedule extends Entity<String> implements EntityValidator {

    private final Movie movie;
    private final LocalDate startAtDate;
    private final LocalTime startAtTime;
    private final Room room;

    public MovieSchedule(String id, Movie movie, LocalDate startAtDate, LocalTime startAtTime, Long roomNumber) {
        super(id);
        this.movie = movie;
        this.startAtDate = startAtDate;
        this.startAtTime = startAtTime;
        this.room = new Room((Room) DatabaseContext.getDatabase(Room.class).get(Long.toString(roomNumber)));
    }

    public Movie getMovie() {
        return movie;
    }

    public LocalTime getStartAtTime() {
        return startAtTime;
    }

    public LocalDate getStartAtDate() {
        return startAtDate;
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return this.getId() + "$" + movie.getId() + "$" +
                startAtDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "$" +
                startAtTime.format(DateTimeFormatter.ofPattern("hh:mm")) + "$" +
                room.getRoomNumber() + '\n';
    }

    @Override
    public void validate() {
        //영화 시작 시간이 0분 혹은 30분인지 확인
        if (startAtTime.getMinute() % 30 != 0) {
            throw new EntityInstantiateException();
        }
    }
}
