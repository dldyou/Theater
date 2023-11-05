package literal;

public class LiteralRegex {
    public static final String MAIN_INPUT = "\\s*[0-4]\\s*$";
    public static final String MANAGE_INPUT = "\\s*[0-57-9]\\s*$";
    public static final String PAGE_NO_OPTION_INPUT = "\\s*[0-57-8]\\s*$";
    public static final String RESERVATION_DATE_INPUT = "\\s*[0-7]\\s*$";

    public static final String MOVIE_NAME = "\\s*.*\\s*";
    public static final String RUNNING_TIME = "\\s*[1-9][0-9]{0,2}\\s*";
    public static final String MOVIE_DATE = "\\s*[0-9]{4}-[0-9]{2}-[0-9]{2}\\s*";
    public static final String FILE_DELIMITER = "(\\r\\n)|(\\n)|(\\r)|(\\$)";
    public static final String CONSOLE_DELIMITER = "(\\r\\n)|(\\n)|(\\r)";
    public static final String MOVIE_ID = "\\s*[A-Za-z0-9]{4}\\s*";
    public static final String START_TIME = "\\s*([0-1][0-9]|2[0-3]):(00|30)\\s*";
    public static final String RESERVATION_CODE = "\\s*0|([A-Z0-9]{6})\\s*";
    public static final String SEAT_NUMBER = "\\s*0|([A-Z](0[1-9]|[1-9][0-9]))\\s*";
    public static final String SYSTEM_TIME = "\\s*[0-9]{4}-[0-9]{2}-[0-9]{2}-([0-1][0-9]|2[0-3])-[0-5][0-9]\\s*";
    public static final String PHONE_NUMBER = "\\s*0|(010-[0-9]{4}-[0-9]{4})\\s*";
}