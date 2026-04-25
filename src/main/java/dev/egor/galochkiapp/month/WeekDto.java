package dev.egor.galochkiapp.month;

import java.time.LocalDate;
import java.util.List;

public record WeekDto(
        LocalDate startDate,
        LocalDate endDate,
        List<DayDto> days
) {
}