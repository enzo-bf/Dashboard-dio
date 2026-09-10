package com.board.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class DateTimes {

    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private DateTimes() {
    }

    public static String format(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return DISPLAY_FORMATTER.format(value);
    }

    public static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " é obrigatório.");
        }
        return value.trim();
    }

    public static boolean equalsIgnoreCaseSafe(String left, String right) {
        return Objects.equals(
                left == null ? null : left.trim().toLowerCase(),
                right == null ? null : right.trim().toLowerCase()
        );
    }
}
