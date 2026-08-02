package dev.egor.galochkiapp.month;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WeekSummaryDto(LocalDate weekStartDate, BigDecimal weekTotal, BigDecimal incomingOverhead, List<DaySummaryDto> days) {
}