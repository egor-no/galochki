package dev.egor.galochkiapp.month;

import java.time.LocalDate;

public record DayDto(
        LocalDate date,
        int dayOfMonth,
        boolean currentMonth
) {
}