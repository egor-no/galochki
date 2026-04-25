package dev.egor.galochkiapp.month;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityService;
import dev.egor.galochkiapp.galochka.Galochka;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
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
    private final GalochkiPageService pageService;

    public MonthPageService(ActivityService activityService,
                            GalochkaRepository galochkaRepository,
                            GalochkiPageService pageService) {
        this.activityService = activityService;
        this.galochkaRepository = galochkaRepository;
        this.pageService = pageService;
    }

    public MonthPageDto build(Long pageId, YearMonth yearMonth) {
        GalochkiPage page = pageService.getById(pageId);

        List<Activity> activities = activityService.getActiveActivitiesByPage(pageId);

        DayOfWeek weekStartDay = page.getWeekStartDay();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Galochka> galochki =
                galochkaRepository.findByActivityPageIdAndDateBetween(pageId, start, end);

        Map<String, Galochka> galochkaMap = galochki.stream()
                .collect(Collectors.toMap(
                        g -> key(g.getActivity().getId(), g.getDate()),
                        Function.identity()
                ));

        List<DayDto> days = buildDays(yearMonth);
        List<ActivityRowDto> rows = buildRows(activities, days, galochkaMap);

        return new MonthPageDto(
                page.getId(),
                page.getTitle(),
                yearMonth,
                yearMonth.minusMonths(1),
                yearMonth.plusMonths(1),
                days,
                rows
        );
    }

    private List<DayDto> buildDays(YearMonth yearMonth) {
        List<DayDto> days = new ArrayList<>();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);

            days.add(new DayDto(
                    date,
                    day,
                    true
            ));
        }

        return days;
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