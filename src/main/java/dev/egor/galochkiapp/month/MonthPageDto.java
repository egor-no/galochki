package dev.egor.galochkiapp.month;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthPageDto(
        Long pageId,
        String pageTitle,
        BigDecimal weeklyNorm,
        YearMonth yearMonth,
        YearMonth previousMonth,
        YearMonth nextMonth,
        List<WeekDto> weeks,
        List<ActivityRowDto> rows,
        List<PageOptionDto> pageOptions,
        List<ActivityGroupDto> groups,
        List<WeekSummaryDto> weekSummaries
) {
}