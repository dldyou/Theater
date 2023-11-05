package service;

import entity.MovieSchedule;
import entity.Ticket;
import entity.TicketDto;
import java.util.Comparator;
import java.util.stream.Collectors;
import repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public String addReservation(MovieSchedule movieSchedule, String seatId, String phoneNumber,
                               LocalDateTime reservationTime) {

        return ticketRepository.save(new Ticket(null, movieSchedule, seatId, phoneNumber, reservationTime, reservationTime, 0));
    }

    //cancelReservation함수는 예매 취소가 성공하는 경우 true를, 실패하는 경우 false를 반환합니다.
    public boolean cancelReservation(String id, LocalDateTime cancellationTime) {

        if (this.ticketRepository.findById(id).isPresent()) {
            return this.ticketRepository.findById(id).get().cancel(cancellationTime);
        } else {
            return false;
        }
    }

    //reservation.txt를 돌면서 주어진 전화번호와 동일한 티켓리스트 리턴
    public List<Ticket> getReservationDetails(String phoneNumber) {
        return ticketRepository.findAllTicketsByPhoneNumber(phoneNumber);
    }

    public List<TicketDto> getTicketStatus(String phoneNumber) {
        List<TicketDto> ticketList = new ArrayList<>();
        ticketRepository.findAllTicketsByPhoneNumber(phoneNumber)
                .forEach(ticket -> {
                    ticketList.add(TicketDto.fromReservedTicket(ticket));
                    if (ticket.isCanceled()) {
                        ticketList.add(TicketDto.fromCancelledTicket(ticket));
                    }
                });
        return ticketList.stream()
                .sorted(Comparator.comparing(TicketDto::getLastModified))
                .collect(Collectors.toList());
    }
}
