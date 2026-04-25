package dev.egor.galochkiapp.month;

import dev.egor.galochkiapp.activity.Activity;

import java.time.YearMonth;
import java.util.List;

public record MonthPageDto(
        YearMonth yearMonth,
        List<WeekDto> weeks,
        List<Activity> activities
) {
}