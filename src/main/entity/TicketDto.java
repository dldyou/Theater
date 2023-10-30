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
        return ticketDto;
    }

    public static TicketDto fromReservedTicket(Ticket ticket) {
        final TicketDto ticketDto = from(ticket);
        ticketDto.lastModified = ticket.getReservationTime();
        return ticketDto;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }
}