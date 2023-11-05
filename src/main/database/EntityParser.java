package database;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EntityParser {

    public static Map<Class<?>, Method> map = new HashMap<>();

    static {
        for (Method m : EntityParser.class.getDeclaredMethods()) {
            if (m.getName().startsWith("parse")) {
                map.put(m.getReturnType(), m);
            }
        }
    }

    public static int parseInt(String data) {
        return Integer.parseInt(data);
    }

    public static Long parseLong(String data) {
        return Long.parseLong(data);
    }

    public static LocalDate parseDate(String data) {
        return LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static LocalTime parseTime(String data) {
        return LocalTime.parse(data, DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static LocalDateTime parseDateTime(String data) {
        return LocalDateTime.parse(data);
    }

    public static String parseString(String data) {
        return data;
    }
}
