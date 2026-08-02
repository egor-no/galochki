package dev.egor.galochkiapp.month;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DaySummaryDto(LocalDate date, BigDecimal total) {
}