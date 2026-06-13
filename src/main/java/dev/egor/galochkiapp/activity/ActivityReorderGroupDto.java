package dev.egor.galochkiapp.activity;

import java.util.List;

public record ActivityReorderGroupDto(
        Long groupId,
        List<Long> activityIds
) {
}