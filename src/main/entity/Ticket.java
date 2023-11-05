package entity;

import java.time.LocalDateTime;
import java.util.StringJoiner;

public class Ticket extends Entity<String> {

    private final MovieSchedule movieSchedule;
    private final String seatId;
    private final String phoneNumber;
    private int isCanceled;
    private final LocalDateTime reservationTime;
    private LocalDateTime cancellationTime;


    public Ticket(String Id, MovieSchedule movieSchedule, String seatId, String phoneNumber,
                  LocalDateTime reservationTime, LocalDateTime cancellationTime, int isCanceled) {
        super(Id);
        this.movieSchedule = movieSchedule;
        this.seatId = seatId;
        this.phoneNumber = phoneNumber;
        this.reservationTime = reservationTime;
        this.cancellationTime = cancellationTime;
        this.isCanceled = isCanceled; //1이면 취소된 경우, 생성할 땐 0
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("$");
        sj.add(id);
        sj.add(movieSchedule.getId());
        sj.add(seatId);
        sj.add(phoneNumber);
        sj.add(reservationTime.toString());
        sj.add(cancellationTime.toString());
        sj.add(Integer.toString(isCanceled));
        return sj.toString() + '\n';
    }

    public MovieSchedule getMovieSchedule() {
        return movieSchedule;
    }

    public String getSeatId() {
        return seatId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isCanceled() {
        return this.isCanceled == 1;
    }

    public LocalDateTime getReservationTime() {
        return this.reservationTime;
    }

    public LocalDateTime getCancellationTime() {
        return this.cancellationTime;
    }

    public boolean cancel(LocalDateTime cancellationTime) {
        if (LocalDateTime.of(this.movieSchedule.getStartAtDate(), this.movieSchedule.getStartAtTime())
                .isAfter(cancellationTime)) {
            this.isCanceled = 1;
            this.cancellationTime = cancellationTime;
            return true;
        } else
            return false;
    }
}
