package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TicketDto {

    boolean isCancelled;
    LocalDateTime lastModified;
    String movieTitle;
    LocalDate scheduleDate;
    LocalTime scheduleTime;
    String roomNumber;
    String seatId;

    private TicketDto(boolean isCancelled, LocalDateTime lastModified, String movieTitle, LocalDate scheduleDate,
                     LocalTime scheduleTime, String roomNumber, String seatId) {
        this.isCancelled = isCancelled;
        this.lastModified = lastModified;
        this.movieTitle = movieTitle;
        this.scheduleDate = scheduleDate;
        this.scheduleTime = scheduleTime;
        this.roomNumber = roomNumber;
        this.seatId = seatId;
    }

    private static TicketDto from(Ticket ticket) {
        final MovieSchedule movieSchedule = ticket.getMovieSchedule();
        final Movie movie = movieSchedule.getMovie();

        return new TicketDto(
                ticket.isCanceled(),
                null,
                movie.getName(),
                movieSchedule.getStartAtDate(),
                movieSchedule.getStartAtTime(),
                movieSchedule.getRoom().getRoomNumber(),
                ticket.getSeatId()
        );
    }

    public static TicketDto fromCancelledTicket(Ticket ticket) {
        final TicketDto ticketDto = from(ticket);
        ticketDto.lastModified = ticket.getCancellationTime();
        ticketDto.isCancelled = true;
        return ticketDto;
    }

    public static TicketDto fromReservedTicket(Ticket ticket) {
        final TicketDto ticketDto = from(ticket);
        ticketDto.lastModified = ticket.getReservationTime();
        ticketDto.isCancelled = false;
        return ticketDto;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public LocalDate getStartAtDate() {
        return scheduleDate;
    }

    public LocalTime getStartAtTime() {
        return scheduleTime;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getSeatNumber() {
        return seatId;
    }

    public String isCanceledOrReserved() {
        return isCancelled ? "취소" : "예매";
    }
}
