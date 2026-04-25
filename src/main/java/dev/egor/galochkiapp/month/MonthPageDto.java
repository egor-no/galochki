package dev.egor.galochkiapp.month;

import java.time.YearMonth;
import java.util.List;

public record MonthPageDto(
        YearMonth yearMonth,
        List<WeekDto> weeks
) {
}