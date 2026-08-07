package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.month.WeekSummaryDto;
import java.util.List;

public record GalochkaUpdateDto(String value, List<WeekSummaryDto> weekSummaries) {
}