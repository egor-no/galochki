package dev.egor.galochkiapp.month;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonthPageService {

    private final ActivityService activityService;

    public MonthPageService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public MonthPageDto buildCurrentMonth() {
        return build(YearMonth.now());
    }

    public MonthPageDto build(YearMonth yearMonth) {
        List<Activity> activities = activityService.getActiveActivities();
        List<WeekDto> weeks = buildWeeks(yearMonth);

        return new MonthPageDto(yearMonth, weeks, activities);
    }

    private List<WeekDto> buildWeeks(YearMonth yearMonth) {
        List<WeekDto> weeks = new ArrayList<>();

        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        LocalDate cursor = firstDay;

        while (cursor.getDayOfWeek() != DayOfWeek.MONDAY) {
            cursor = cursor.minusDays(1);
        }

        while (!cursor.isAfter(lastDay)) {
            List<DayDto> days = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                days.add(new DayDto(
                        cursor,
                        cursor.getDayOfMonth(),
                        YearMonth.from(cursor).equals(yearMonth)
                ));

                cursor = cursor.plusDays(1);
            }

            weeks.add(new WeekDto(days));
        }

        return weeks;
    }
}