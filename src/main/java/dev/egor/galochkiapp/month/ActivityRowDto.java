package dev.egor.galochkiapp.month;

import java.util.List;

public record ActivityRowDto(
        Long activityId,
        String title,
        List<ActivityWeekCellsDto> weeks
) {
}