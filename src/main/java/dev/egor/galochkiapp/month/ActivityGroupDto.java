package dev.egor.galochkiapp.month;

import java.util.List;

public record ActivityGroupDto(
        Long groupId,
        String title,
        List<ActivityRowDto> rows
) {
}