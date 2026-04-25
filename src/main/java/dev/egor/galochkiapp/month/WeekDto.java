package dev.egor.galochkiapp.month;

import java.util.List;

public record WeekDto(
        List<DayDto> days,
        List<ActivityRowDto> rows
) {
}