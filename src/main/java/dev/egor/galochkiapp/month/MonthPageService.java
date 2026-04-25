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
        GalochkiPage page = pageService.getByIdForCurrentOwner(pageId);

        List<Activity> activities = activityService.getActiveActivitiesByPageForCurrentOwner(pageId);

        DayOfWeek weekStartDay = page.getWeekStartDay();

        List<WeekDto> weeks = buildWeeks(yearMonth, weekStartDay);

        LocalDate start = weeks.get(0).startDate();
        LocalDate end = weeks.get(weeks.size() - 1).endDate();

        List<Galochka> galochki =
                galochkaRepository.findByActivityPageIdAndDateBetween(pageId, start, end);

        Map<String, Galochka> galochkaMap = galochki.stream()
                .collect(Collectors.toMap(
                        g -> key(g.getActivity().getId(), g.getDate()),
                        Function.identity()
                ));

        List<ActivityRowDto> rows = buildRows(activities, weeks, galochkaMap);

        List<PageOptionDto> pageOptions = pageService.getAllPagesForCurrentOwner().stream()
                .map(p -> new PageOptionDto(p.getId(), p.getTitle()))
                .toList();

        return new MonthPageDto(
                page.getId(),
                page.getTitle(),
                yearMonth,
                yearMonth.minusMonths(1),
                yearMonth.plusMonths(1),
                weeks,
                rows,
                pageOptions
        );
    }

    private List<WeekDto> buildWeeks(YearMonth yearMonth, DayOfWeek weekStartDay) {
        List<WeekDto> weeks = new ArrayList<>();

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

        LocalDate cursor = moveBackToWeekStart(firstDayOfMonth, weekStartDay);

        while (!cursor.isAfter(lastDayOfMonth)) {
            List<DayDto> days = new ArrayList<>();

            LocalDate weekStart = cursor;

            for (int i = 0; i < 7; i++) {
                days.add(new DayDto(
                        cursor,
                        cursor.getDayOfMonth(),
                        YearMonth.from(cursor).equals(yearMonth)
                ));

                cursor = cursor.plusDays(1);
            }

            weeks.add(new WeekDto(
                    weekStart,
                    cursor.minusDays(1),
                    days
            ));
        }

        return weeks;
    }

    private LocalDate moveBackToWeekStart(LocalDate date, DayOfWeek weekStartDay) {
        LocalDate result = date;

        while (result.getDayOfWeek() != weekStartDay) {
            result = result.minusDays(1);
        }

        return result;
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
                                           List<WeekDto> weeks,
                                           Map<String, Galochka> galochkaMap) {
        List<ActivityRowDto> rows = new ArrayList<>();

        for (Activity activity : activities) {
            List<ActivityWeekCellsDto> activityWeeks = new ArrayList<>();

            for (WeekDto week : weeks) {
                List<GalochkaCellDto> cells = new ArrayList<>();

                for (DayDto day : week.days()) {
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

                activityWeeks.add(new ActivityWeekCellsDto(cells));
            }

            rows.add(new ActivityRowDto(
                    activity.getId(),
                    activity.getTitle(),
                    activityWeeks
            ));
        }

        return rows;
    }

    private String key(Long activityId, LocalDate date) {
        return activityId + "_" + date;
    }
}