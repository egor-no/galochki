package dev.egor.galochkiapp.month;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityService;
import dev.egor.galochkiapp.galochka.Galochka;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MonthPageService {

    private final ActivityService activityService;
    private final GalochkaRepository galochkaRepository;

    public MonthPageService(ActivityService activityService,
                            GalochkaRepository galochkaRepository) {
        this.activityService = activityService;
        this.galochkaRepository = galochkaRepository;
    }

    public MonthPageDto buildCurrentMonth() {
        return build(YearMonth.now());
    }

    public MonthPageDto build(YearMonth yearMonth) {
        List<Activity> activities = activityService.getActiveActivities();

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Galochka> galochki = galochkaRepository.findByDateBetween(start, end);

        Map<String, Galochka> galochkaMap = galochki.stream()
                .collect(Collectors.toMap(
                        g -> key(g.getActivity().getId(), g.getDate()),
                        Function.identity()
                ));

        List<WeekDto> weeks = buildWeeks(yearMonth, activities, galochkaMap);

        return new MonthPageDto(yearMonth, weeks);
    }

    private List<WeekDto> buildWeeks(YearMonth yearMonth,
                                     List<Activity> activities,
                                     Map<String, Galochka> galochkaMap) {
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

            List<ActivityRowDto> rows = buildRows(activities, days, galochkaMap);

            weeks.add(new WeekDto(days, rows));
        }

        return weeks;
    }

    private List<ActivityRowDto> buildRows(List<Activity> activities,
                                           List<DayDto> days,
                                           Map<String, Galochka> galochkaMap) {
        List<ActivityRowDto> rows = new ArrayList<>();

        for (Activity activity : activities) {
            List<GalochkaCellDto> cells = new ArrayList<>();

            for (DayDto day : days) {
                Galochka galochka = galochkaMap.get(key(activity.getId(), day.date()));

                BigDecimal value = galochka == null
                        ? BigDecimal.ZERO
                        : galochka.getValue();

                cells.add(new GalochkaCellDto(
                        activity.getId(),
                        day.date().toString(),
                        value.stripTrailingZeros().toPlainString()
                ));
            }

            rows.add(new ActivityRowDto(
                    activity.getId(),
                    activity.getTitle(),
                    cells
            ));
        }

        return rows;
    }

    private String key(Long activityId, LocalDate date) {
        return activityId + "_" + date;
    }
}