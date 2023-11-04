package util;

import entity.Movie;
import entity.MovieSchedule;
import entity.Room;
import entity.Ticket;
import entity.TicketDto;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import literal.LiteralRegex;
import literal.Literals;
import literal.Path;
import repository.MovieRepository;
import repository.MovieScheduleRepository;
import repository.RoomRepository;
import repository.TicketRepository;
import service.MovieScheduleService;
import service.MovieService;
import service.RoomService;
import service.TicketService;


public class Console {

    private final Input input;
    private final MovieService movieService;
    private final MovieScheduleService movieScheduleService;
    private final TicketService ticketService;
    private final RoomService roomService;
    private LocalDateTime systemTime;

    public Console() {
        input = new Input(System.in, LiteralRegex.CONSOLE_DELIMITER);
        roomService = new RoomService(new RoomRepository(Path.THEATER_DATA));
        movieService = new MovieService(new MovieRepository(Path.MOVIE_DATA));
        movieScheduleService = new MovieScheduleService(new MovieScheduleRepository(Path.SCHEDULE_DATA));
        ticketService = new TicketService(new TicketRepository(Path.RESERVATION_DATA));
    }

    public int mainMenu() {
        while (true) {
            String command = "";
            printf("============== 메인메뉴 ==============\n" +
                    "1. 영화관리\n" +
                    "2. 예매\n" +
                    "3. 예매취소\n" +
                    "4. 예매 내역 조회\n" +
                    "0. 종료 \n" +
                    "입력: ");
            if ((command = input.getByPattern(LiteralRegex.MAIN_INPUT)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }
            switch (command) {
                case Literals.QUIT:
                    return exit();
                case Literals.RESERVATION: {
                    reservationMenu();
                    break;
                }
                case Literals.MANAGEMENT: {
                    managementMenu();
                    break;
                }
                case Literals.CANCEL_RESERVATION: {
                    cancelReservationMenu();
                    break;
                }
                case Literals.CHECK_RESERVATION: {
                    checkReservationDetailsMenu();
                    break;
                }
            }
        }
    }

    /* 부 프롬프트 1: 영화 관리 */
    private void managementMenu() {
        int nextMenu = 0;
        int page = 1;
        int totalPage = movieService.getTotalPages();
        List<Movie> movies = movieService.getSortedMovies();
        String command = "";

        while (true) {
            command = "";
            StringBuilder sb = new StringBuilder("============== 영화관리 ==============\n");
            for (int i = (page - 1) * 5, idx = 1; idx <= 5 && i < movies.size(); i++, idx++) {
                sb.append(idx).append(". ").append(movies.get(i).getName()).append("\n");
            }
            sb.append(String.format("=========== 페이지 %d / %d ===========\n", page, totalPage));
            sb.append("7. 이전 페이지\n8. 다음 페이지\n9. 영화 추가\n0. 뒤로가기\n입력: ");
            printf(sb.toString());

            if ((command = input.getByPattern(LiteralRegex.MANAGE_INPUT)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            switch (command) {
                case "1":
                case "2":
                case "3":
                case "4":
                case "5": {
                    int num = Integer.parseInt(command);
                    if (!checkValidMovieNumber(movies, page, num)) {
                        printError("해당 영화가 존재하지 않습니다. 다시 입력해 주세요.");
                        continue;
                    } else {
                        nextMenu = 2;
                    }
                    break;
                }

                case Literals.PREVIOUS_PAGE: {
                    if (movieService.hasPreviousPage(page)) {
                        page--;
                    } else {
                        printError("이전 페이지가 존재하지 않습니다.");
                    }
                    break;
                }

                case Literals.NEXT_PAGE: {
                    if (movieService.hasNextPage(page)) {
                        page++;
                    } else {
                        printError("다음 페이지가 존재하지 않습니다.\n");
                    }
                    break;
                }

                case Literals.ADD_MOVIE: {
                    nextMenu = 1;
                    break;
                }
                case Literals.BACK:
                    return;
            }

            if (nextMenu != 0) {
                break;
            }
        }

        if (nextMenu == 1) {
            addMovieMenu();
        } else if (nextMenu == 2) {
            addMovieScheduleMenu(new MovieSchedule(
                    null,
                    movies.get(getPageNumber(page, Integer.parseInt(command)) - 1),
                    LocalDate.now(),
                    LocalTime.now(),
                    1L));
        }
    }

    /* 부 프롬프트 1.1: 영화 추가 */
    private void addMovieMenu() {
        while (true) {
            printf("영화 이름을 입력해주세요 : ");
            String movieName = input.getByPattern(LiteralRegex.MOVIE_NAME).trim();
            if (movieName.contains("$")) {
                printError("$는 포함될 수 없는 문자입니다. 다시 입력해 주세요.");
                continue;
            }
            printf("러닝타임을 입력해주세요 : ");
            String runningTimeStr = input.getByPattern(LiteralRegex.RUNNING_TIME);
            if (runningTimeStr == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }
            int runningTime = Integer.parseInt(runningTimeStr);
            movieService.addMovie(movieName, runningTime);
            println("영화를 저장했습니다.");
            break;
        }
        managementMenu();
    }

    /* 부 프롬프트 1.2: 영화 상영 일정 추가 */
    private void addMovieScheduleMenu(MovieSchedule movieSchedule) {
        int nextMenu = 0;
        int page = 1;
        int totalPage = roomService.getTotalPages();
        List<Room> rooms = roomService.getSortedRooms();
        String command = "";

        while (true) {
            command = "";
            StringBuilder sb = new StringBuilder("============== 일정추가 ==============\n");
            for (int i = (page - 1) * 5, idx = 1; idx <= 5 && i < rooms.size(); i++, idx++) {
                sb.append(idx).append(". ").append(rooms.get(i).getRoomNumber()).append("관\n");
            }
            sb.append(String.format("=========== 페이지 %d / %d ===========\n", page, totalPage));
            sb.append("7. 이전 페이지\n8. 다음 페이지\n0. 뒤로가기\n입력: ");
            printf(sb.toString());

            if ((command = input.getByPattern(LiteralRegex.PAGE_NO_OPTION_INPUT)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            switch (command) {
                case "1":
                case "2":
                case "3":
                case "4":
                case "5": {
                    int num = Integer.parseInt(command);
                    if (!checkValidRoomNumber(rooms, page, num)) {
                        printError("해당 관이 존재하지 않습니다. 다시 입력해 주세요.");
                        continue;
                    } else {
                        nextMenu = 1;
                    }
                    break;
                }

                case Literals.PREVIOUS_PAGE: {
                    if (roomService.hasPreviousPage(page)) {
                        page--;
                    } else {
                        printError("이전 페이지가 존재하지 않습니다.");
                    }
                    break;
                }

                case Literals.NEXT_PAGE: {
                    if (roomService.hasNextPage(page)) {
                        page++;
                    } else {
                        printError("다음 페이지가 존재하지 않습니다.\n");
                    }
                    break;
                }

                case Literals.BACK: {
                    nextMenu = -1;
                    break;
                }
            }

            if (nextMenu != 0) {
                break;
            }
        }
        if (nextMenu == -1) {
            managementMenu();
        } else if (nextMenu == 1) {
            selectMovieDateMenu(new MovieSchedule(
                    movieSchedule.getId(),
                    movieSchedule.getMovie(),
                    movieSchedule.getStartAtDate(),
                    movieSchedule.getStartAtTime(),
                    Long.parseLong(rooms.get(getPageNumber(page, Integer.parseInt(command)) - 1).getRoomNumber())));
        }
    }

    /* 부 프롬프트 1.3: 영화 상영일 선택 */
    private void selectMovieDateMenu(MovieSchedule movieSchedule) {
        String command = "";
        while (true) {
            command = "";
            printf("============== 일정추가 ==============\n" +
                    "입력 (YYYY-MM-DD 형식): ");
            if ((command = input.getByPattern(LiteralRegex.MOVIE_DATE)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            try {
                /* 실제로 존재하는 날짜라면 과거가 아닌지, 유효기간 내에 존재하는지 테스트 */
                LocalDate curDate = LocalDate.of(systemTime.getYear(), systemTime.getMonthValue(),
                        systemTime.getDayOfMonth());
                LocalDate inpDate = LocalDate.parse(command);
                LocalDate endDate = LocalDate.parse("2100-12-31");

                if (inpDate.compareTo(curDate) < 0) {
                    printError("지난 날짜입니다. 다시 입력해주세요.\n");
                    continue;
                } else if (endDate.compareTo(inpDate) < 0) {
                    printError("2100년 이내의 날짜여야 합니다. 다시 입력해주세요.\n");
                    continue;
                }
            } catch (DateTimeException e) {
                printError("존재하지 않는 날짜입니다. 다시 입력해주세요.\n");
                continue;
            }

            break;
        }
        selectMovieTimeMenu(new MovieSchedule(
                    movieSchedule.getId(),
                    movieSchedule.getMovie(),
                    LocalDate.parse(command),
                    movieSchedule.getStartAtTime(),
                    Long.parseLong(movieSchedule.getRoom().getRoomNumber())));
    }

    /* 부 프롬프트 1.4: 영화 시작 시각 입력 */
    private void selectMovieTimeMenu(MovieSchedule movieSchedule) {
        String command = "";
        List<Movie> movies = movieScheduleService.getDistinctMoviesByStartDateAtAndRoomNumber(
                movieSchedule.getStartAtDate(),
                Long.parseLong(movieSchedule.getRoom().getRoomNumber()));

        Boolean startTimes[] = new Boolean[48], startTimesOfNextDay[] = new Boolean[48];
        for (int i = 0; i < 48; i++) {
            startTimes[i] = startTimesOfNextDay[i] = false;
        }

        for (Movie movie : movies) {
            Boolean startTimesTemp[] = movieScheduleService.getMovieStartTimes(
                    movie,
                    movieSchedule.getStartAtDate(),
                    Long.parseLong(movieSchedule.getRoom().getRoomNumber()));

            Boolean startTimesOfNextDayTemp[] = movieScheduleService.getMovieStartTimes(
                    movie,
                    movieSchedule.getStartAtDate().plusDays(1),
                    Long.parseLong(movieSchedule.getRoom().getRoomNumber()));

            for (int i = 0; i < 48; i++) {
                startTimes[i] |= startTimesTemp[i];
                startTimesOfNextDay[i] |= startTimesOfNextDayTemp[i];
            }
        }

        while (true) {
            StringBuilder sb = new StringBuilder(String.format(
                    "============== %s 상영 시각 추가 ==============\n", movieSchedule.getStartAtDate()));
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 12; j++) {
                    sb.append(i * 12 + j < 10 ? "0" : "").append(i * 12 + j).append("    ");
                }
                sb.append("\n");
                for (int j = 0; j < 24; j++) {
                    sb.append(startTimes[i * 24 + j] ? "■" : "□").append(" ");
                }
                sb.append("\n");
            }
            sb.append("상영 시작 시각 (HH:MM 형식): ");
            printf(sb.toString());

            if ((command = input.getByPattern(LiteralRegex.START_TIME)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            // 과거의 시간인지
            LocalDateTime curDateTime = systemTime;
            LocalDateTime inpDateTime = LocalDateTime.of(movieSchedule.getStartAtDate(), LocalTime.parse(command));
            if (inpDateTime.compareTo(curDateTime) < 0) {
                printError("지난 시간입니다. 다시 입력해주세요.\n");
                continue;
            }

            // 추가하려는 시간 ~ 러닝타임 동안 상영 중인 영화가 있어서 추가가 불가능한 경우
            int start_h = Integer.parseInt(command.split(":")[0]);
            int start_m = Integer.parseInt(command.split(":")[1]);

            int end_time = start_h * 60 + start_m + movieSchedule.getMovie().getRunningTime();
            int end_h = end_time / 60;
            int end_m = end_time % 60;

            int s = start_h * 2 + (start_m == 30 ? 1 : 0);
            int e = end_h * 2 + (end_m > 30 ? 2 : (end_m > 0 ? 1 : 0));

            boolean flag = false;
            for (int i = s; i < e; i++) {
                if (i < 48) {
                    flag |= startTimes[i];
                } else {
                    flag |= startTimesOfNextDay[i - 48];
                }
            }

            if (flag) {
                printError("해당 시간에 상영 시각을 추가할 수 없습니다. 다시 입력해주세요.");
                continue;
            }

            break;
        }

        movieScheduleService.addMovieSchedule(
                movieSchedule.getId(),
                movieSchedule.getMovie(),
                movieSchedule.getStartAtDate(),
                LocalTime.parse(command),
                Long.parseLong(movieSchedule.getRoom().getRoomNumber()));

        managementMenu();
    }

    private void removeMovieMenu() {

    }

    /* 부 프롬프트 2: 예매 */
    private void reservationMenu() {
        int nextMenu = 0;
        int page = 1;
        int totalPage = movieService.getTotalPages();
        List<Movie> movies = movieService.getSortedMovies();
        String command = "";

        while (true) {
            StringBuilder sb = new StringBuilder("============== 영화예매 ==============\n");
            for (int i = (page - 1) * 5, idx = 1; idx <= 5 && i < movies.size(); i++, idx++) {
                sb.append(idx).append(". ").append(movies.get(i).getName()).append("\n");
            }
            sb.append(String.format("=========== 페이지 %d / %d ===========\n", page, totalPage));
            sb.append("7. 이전 페이지\n8. 다음 페이지\n0. 뒤로가기\n입력: ");
            printf(sb.toString());

            if ((command = input.getByPattern(LiteralRegex.PAGE_NO_OPTION_INPUT)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            switch (command) {
                case "1":
                case "2":
                case "3":
                case "4":
                case "5": {
                    int num = Integer.parseInt(command);
                    if (!checkValidMovieNumber(movies, page, num)) {
                        printError("해당 영화가 존재하지 않습니다. 다시 입력해 주세요.");
                        continue;
                    } else {
                        nextMenu = 1;
                        break;
                    }
                }

                case Literals.PREVIOUS_PAGE: {
                    if (movieService.hasPreviousPage(page)) {
                        page--;
                    } else {
                        printError("이전 페이지가 존재하지 않습니다.");
                    }
                    break;
                }

                case Literals.NEXT_PAGE: {
                    if (movieService.hasNextPage(page)) {
                        page++;
                    } else {
                        printError("다음 페이지가 존재하지 않습니다.\n");
                    }
                    break;
                }

                case Literals.BACK: 
                    return;
            }
            if (nextMenu != 0) {
                break;
            }
        }
        if (nextMenu == 1) {
            int num = Integer.parseInt(command);
            selectReservationDateMenu(new Ticket(
                    null,
                    new MovieSchedule(
                            null,
                            movies.get(getPageNumber(page, num) - 1),
                            systemTime.toLocalDate(),
                            systemTime.toLocalTime(),
                            1L),
                null,
                null,
                systemTime
            ));
        }
    }

    /* 부 프롬프트 2.1: 예매 날짜 선택 */
    private void selectReservationDateMenu(Ticket ticket) {
        int nextMenu = 0;
        String command = "";
        while (true) {
            StringBuilder sb = new StringBuilder("============== 날짜선택 ==============\n");
            for (int i = 0; i < 7; i++) {
                LocalDateTime dates = systemTime.plusDays(i);
                sb.append(
                        String.format("%d. %s\n", i + 1, dates.toLocalDate().toString() + (i == 0 ? " (Today)" : "")));
            }
            sb.append("0. 뒤로가기\n").append("입력: ");
            printf(sb.toString());

            if ((command = input.getByPattern(LiteralRegex.RESERVATION_DATE_INPUT)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            if (command.equals(Literals.BACK)) 
                nextMenu = -1;
            else
                nextMenu = 1;

            break;
        }
        if (nextMenu == -1) 
            reservationMenu();
        else if (nextMenu == 1) {
            int num = Integer.parseInt(command);
            selectReservationRoomMenu(new Ticket(
                ticket.getId(),
                new MovieSchedule(
                ticket.getMovieSchedule().getId(), 
                ticket.getMovieSchedule().getMovie(), 
                systemTime.plusDays(num - 1).toLocalDate(), 
                ticket.getMovieSchedule().getStartAtTime(), 
                Long.parseLong(ticket.getMovieSchedule().getRoom().getRoomNumber())),
            null,
            null,
            systemTime
            ));
        }
    }

    /* 부 프롬프트 2.2: 상영관 선택 */
    private void selectReservationRoomMenu(Ticket ticket) {
        int nextMenu = 0;
        List<Long> rooms = movieScheduleService.getTheaterByMovieAndDate(
                ticket.getMovieSchedule().getMovie(),
                ticket.getMovieSchedule().getStartAtDate());
        int page = 1;
        int totalPage = rooms.size() / 5 + (rooms.size() % 5 != 0 ? 1 : 0);
        String command = "";

        while (true) {
            command = "";
            StringBuilder sb = new StringBuilder("============== 상영관 선택 ==============\n");
            for (int i = (page - 1) * 5, idx = 1; idx <= 5 && i < rooms.size(); i++, idx++)
                sb.append(idx).append(". ").append(rooms.get(i)).append("관\n");
            sb.append(String.format("=========== 페이지 %d / %d ===========\n", page, totalPage));
            sb.append("7. 이전 페이지\n8. 다음 페이지\n0. 뒤로가기\n입력: ");
            printf(sb.toString());

            if ((command = input.getByPattern(LiteralRegex.PAGE_NO_OPTION_INPUT)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            switch (command) {
                case "1":
                case "2":
                case "3":
                case "4":
                case "5": {
                    int num = Integer.parseInt(command);
                    if (!checkValidNumber(rooms.size(), page, num)) {
                        printError("해당 관이 존재하지 않습니다. 다시 입력해 주세요.");
                        continue;
                    } else 
                        nextMenu = 1;
                    break;
                }

                case Literals.PREVIOUS_PAGE: {
                    if ((page - 1) >= 1)
                        page--;
                    else
                        printError("이전 페이지가 존재하지 않습니다.");
                    break;
                }

                case Literals.NEXT_PAGE: {
                    if ((page + 1) <= totalPage)
                        page++;
                    else
                        printError("다음 페이지가 존재하지 않습니다.\n");
                    break;
                }

                case Literals.BACK: {
                    nextMenu = -1;
                    break;
                }
            }

            if (nextMenu != 0)
                break;
        }
        if (nextMenu == -1) 
            selectReservationDateMenu(ticket);
        else if (nextMenu == 1) {
            int num = Integer.parseInt(command);
            selectReservationTimeMenu(new Ticket(
                ticket.getId(),
                new MovieSchedule(
                ticket.getMovieSchedule().getId(), 
                ticket.getMovieSchedule().getMovie(), 
                ticket.getMovieSchedule().getStartAtDate(), 
                ticket.getMovieSchedule().getStartAtTime(), 
                rooms.get(getPageNumber(page, num) - 1)),
                null,
                null,
                systemTime
            ));
        }
    }

    /* 부 프롬프트 2.3: 일정 선택 */
    private void selectReservationTimeMenu(Ticket ticket) {
        MovieSchedule movieSchedule = ticket.getMovieSchedule();
        List<LocalTime> schedules = movieScheduleService.getMovieStartAtTimeByDateAndRoomNumber(
                    movieSchedule.getMovie().getId(),
                    movieSchedule.getStartAtDate(),
                    Long.parseLong(movieSchedule.getRoom().getRoomNumber()));
        int sz = schedules.size();
        int page = 1;
        int totalPage = sz / 5 + (sz % 5 != 0 ? 1 : 0);
        int nextMenu = 0;
        String command = "";
        
        while (true) {
            command = "";
            StringBuilder sb = new StringBuilder("============== 일정 선택 ==============\n");
            for (int i = (page - 1) * 5, idx = 1; idx <= 5 && i < sz; i++, idx++) {
                LocalTime startAtTime = schedules.get(i);
                LocalTime endAtTime = startAtTime.plusMinutes(ticket.getMovieSchedule().getMovie().getRunningTime());
                Room room = movieScheduleService.getRoomByMovieSchedule(
                        movieSchedule.getMovie(),
                        movieSchedule.getStartAtDate(),
                        startAtTime,
                        movieSchedule.getRoom().getRoomNumber());
                sb.append(idx).append(". ").append(startAtTime.toString()).append("-").append(endAtTime.toString())
                        .append(String.format("[%d / %d]\n", room.getRemainSeats(), room.getTotalSeats()));
            }
            sb.append(String.format("=========== 페이지 %d / %d ===========\n", page, totalPage));
            sb.append("7. 이전 페이지\n8. 다음 페이지\n0. 뒤로가기\n입력: ");
            printf(sb.toString());

            if ((command = input.getByPattern(LiteralRegex.PAGE_NO_OPTION_INPUT)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            switch (command) {
                case "1":
                case "2":
                case "3":
                case "4":
                case "5": {
                    int num = Integer.parseInt(command);
                    if (!checkValidNumber(sz, page, num)) {
                        printError("해당 일정이 존재하지 않습니다. 다시 입력해 주세요.");
                        continue;
                    } else
                        nextMenu = 1;
                    break;
                }

                case Literals.PREVIOUS_PAGE: {
                    if ((page - 1) >= 1)
                        page--;
                    else
                        printError("이전 페이지가 존재하지 않습니다.");
                    break;
                }

                case Literals.NEXT_PAGE: {
                    if ((page + 1) <= totalPage)
                        page++;
                    else
                        printError("다음 페이지가 존재하지 않습니다.\n");
                    break;
                }

                case Literals.BACK: {
                    nextMenu = -1;
                    break;
                }
            }

            if (nextMenu != 0)
                break;
        }
        if (nextMenu == -1) 
            selectReservationRoomMenu(ticket);
        else {
            int num = Integer.parseInt(command);
            selectReservationSeatMenu(new Ticket(
                ticket.getId(),
                new MovieSchedule(
                ticket.getMovieSchedule().getId(), 
                ticket.getMovieSchedule().getMovie(), 
                ticket.getMovieSchedule().getStartAtDate(), 
                schedules.get(getPageNumber(page, num) - 1), 
                Long.parseLong(ticket.getMovieSchedule().getRoom().getRoomNumber())),
                null,
                null,
                systemTime
            ));
        }
    }

    /* 부 프롬프트 2.4: 좌석 선택 */
    private void selectReservationSeatMenu(Ticket ticket) {
        int nextMenu = 0;
        String command = "";
        MovieSchedule movieSchedule = ticket.getMovieSchedule();
        Room room = movieScheduleService.getRoomByMovieSchedule(
                movieSchedule.getMovie(),
                movieSchedule.getStartAtDate(),
                movieSchedule.getStartAtTime(),
                movieSchedule.getRoom().getRoomNumber());
        while (true) {
            command = "";
            StringBuilder sb = new StringBuilder("============== 좌석 선택 ==============\n");
            sb.append(room.getSeatsToString());
            sb.append("입력 (뒤로가기: 0) : ");
            printf(sb.toString());

            if ((command = input.getByPattern(LiteralRegex.SEAT_NUMBER)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }
            
            if (command.equals(Literals.BACK)) {
                nextMenu = -1;
                break;
            } else if (room.canReserveSeat(command)) {
                nextMenu = 1;
                break;
            } else {
                printError("좌석이 존재하지 않거나 예매 불가능한 좌석입니다. 다시 입력해주세요.\n");
            }
        }
        if (nextMenu == -1) {
            selectReservationTimeMenu(ticket);
        }
        else if (nextMenu == 1) {
            inputPhoneNumberMenu(new Ticket(
                ticket.getId(),
                ticket.getMovieSchedule(),
                command,
                null,
                systemTime
            ));
        }
    }

    /* 부 프롬프트 2.5: 전화번호 입력 */
    private void inputPhoneNumberMenu(Ticket ticket) {
        int nextMenu = 0;
        String command = "";
        while (true) {
            println("============== 전화번호 입력 ==============");
            printf("전화번호 입력(뒤로가기: 0): ");

            if ((command = input.getByPattern(LiteralRegex.PHONE_NUMBER)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            if (command.equals(Literals.BACK)) {
                nextMenu = -1;
            } else {
                nextMenu = 1;
            }

            if (nextMenu != 0)
                break;
        }
        if(nextMenu == -1) 
            selectReservationSeatMenu(ticket);
        else if (nextMenu == 1) {
            printReservationCodeMenu(new Ticket(
                ticket.getId(),
                ticket.getMovieSchedule(),
                ticket.getSeatId(),
                command,
                systemTime
            ));
        }
    }

    /* 부 프롬프트 2.6: 예매 코드 출력 */
    private void printReservationCodeMenu(Ticket ticket) {
        println("============== 예매코드 ==============");
        println("예매 코드는 " + ticketService.addReservation(
                ticket.getMovieSchedule(),
                ticket.getSeatId(),
                ticket.getPhoneNumber(),
                ticket.getReservationTime()) + " 입니다.");
    }

    /* 부 프롬프트 3: 예매 취소 */
    private void cancelReservationMenu() {
        String command = "";
        while (true) {
            println("============== 예매취소 ==============");
            printf("예매 코드 입력 (뒤로 가기: 0): ");
            if ((command = input.getByPattern(LiteralRegex.RESERVATION_CODE)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            if (!ticketService.cancelReservation(command, systemTime)) {
                printError("해당 예매 코드가 존재하지 않습니다. 다시 입력해주세요.\n");
                continue;
            } else
                break;
        }
    }

    /* 부 프롬프트 4: 예매 내역 조회 */
    private void checkReservationDetailsMenu() {
        int nextMenu = 0;
        String command = "";
        while (true) {
            println("============== 전화번호 입력 ==============");
            printf("전화번호 입력(뒤로가기: 0): ");

            if ((command = input.getByPattern(LiteralRegex.PHONE_NUMBER)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            if (command.equals(Literals.BACK)) {
                nextMenu = -1;
                break;
            }

            println("예매 내역은 다음과 같습니다.");
            List<TicketDto> tickets = ticketService.getTicketStatus(command);
            for (TicketDto ticket : tickets) {
                println(String.format("(%s %s %s) %s %s %s %s관 %s",
                        ticket.getLastModified().toLocalDate().toString(),
                        ticket.getLastModified().toLocalTime().toString(),
                        ticket.isCanceledOrReserved(),
                        ticket.getMovieTitle(),
                        ticket.getStartAtDate().toString(),
                        ticket.getStartAtTime().toString(),
                        ticket.getRoomNumber(),
                        ticket.getSeatNumber()));
            }
            break;
        }
        if(nextMenu == -1) 
            return;
    }

    /* 시스템 시각 입력 */
    public void inputCurrentTime() {
        String command = "";
        while (true) {
            printf("기준 시각 입력 (yyyy-MM-dd-HH-mm 형식): ");

            if ((command = input.getByPattern(LiteralRegex.SYSTEM_TIME)) == null) {
                printError("입력 형식에 맞지 않습니다. 다시 입력해주세요.\n");
                continue;
            }

            int year = Integer.parseInt(command.split("-")[0]);
            int month = Integer.parseInt(command.split("-")[1]);
            int day = Integer.parseInt(command.split("-")[2]);
            int hour = Integer.parseInt(command.split("-")[3]);
            int minute = Integer.parseInt(command.split("-")[4]);

            try {
                systemTime = LocalDateTime.of(year, month, day, hour, minute);
            } catch (DateTimeException e) {
                printError("올바르지 않는 시각 형식입니다. 다시 입력해주세요.\n");
                continue;
            }

            break;
        }
    }

    public void run() {
        inputCurrentTime();
        mainMenu();
    }

    public int exit() {
        int exitCode = 0;
        exitCode |= input.close();
        return exitCode;
    }

    private void printError(String s) {
        System.err.println("Error : " + s);
        flush();
    }

    private void println(String s) {
        System.out.println(s);
        flush();
    }

    private void printf(String format, Object... args) {
        System.out.printf(format, args);
        flush();
    }

    private void flush() {
        System.err.flush();
        System.out.flush();
    }

    private int getPageNumber(int page, int num) {
        return (page - 1) * 5 + num;
    }

    private boolean checkValidNumber(int size, int page, int num) {
        return getPageNumber(page, num) <= size;
    }

    private boolean checkValidMovieNumber(List<Movie> movies, int page, int num) {
        return getPageNumber(page, num) <= movies.size();
    }

    private boolean checkValidRoomNumber(List<Room> rooms, int page, int num) {
        return getPageNumber(page, num) <= rooms.size();
    }
}
