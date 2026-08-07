package dev.egor.galochkiapp.month;

import dev.egor.galochkiapp.activity.*;
import dev.egor.galochkiapp.galochka.Galochka;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import dev.egor.galochkiapp.week.PageWeekOverheadService;
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
    private final ActivityGroupRepository groupRepository;
    private final GalochkaRepository galochkaRepository;
    private final GalochkiPageService pageService;
    private final PageWeekOverheadService overheadService;

    public MonthPageService(ActivityService activityService,
                            GalochkaRepository galochkaRepository,
                            GalochkiPageService pageService,
                            ActivityGroupRepository groupRepository,
                            PageWeekOverheadService overheadService) {
        this.activityService = activityService;
        this.galochkaRepository = galochkaRepository;
        this.pageService = pageService;
        this.groupRepository = groupRepository;
        this.overheadService = overheadService;
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
        List<ActivityGroupDto> groups = buildGroups(pageId, rows);
        List<WeekSummaryDto> weekSummaries = buildWeekSummaries(pageId, weeks, galochki);

        List<PageOptionDto> pageOptions = pageService.getAllPagesForCurrentOwner().stream()
                .map(p -> new PageOptionDto(p.getId(), p.getTitle()))
                .toList();

        return new MonthPageDto(
                page.getId(),
                page.getTitle(),
                page.getPageType(),
                page.getWeeklyNorm(),
                yearMonth,
                yearMonth.minusMonths(1),
                yearMonth.plusMonths(1),
                weeks,
                rows,
                pageOptions,
                groups,
                weekSummaries
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

    private List<WeekSummaryDto> buildWeekSummaries(Long pageId, List<WeekDto> weeks, List<Galochka> galochki) {
        Map<LocalDate, BigDecimal> totalsByDate = galochki.stream()
                .collect(Collectors.groupingBy(Galochka::getDate, Collectors.reducing(BigDecimal.ZERO, Galochka::getValue, BigDecimal::add)));

        List<WeekSummaryDto> result = new ArrayList<>();

        for (WeekDto week : weeks) {
            List<DaySummaryDto> days = week.days().stream()
                    .map(day -> new DaySummaryDto(day.date(), totalsByDate.getOrDefault(day.date(), BigDecimal.ZERO)))
                    .toList();

            BigDecimal weekTotal = days.stream()
                    .map(DaySummaryDto::total)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal incomingOverhead = overheadService.getIncomingOverhead(pageId, week.startDate());

            result.add(new WeekSummaryDto(week.startDate(), weekTotal, incomingOverhead, days));
        }

        return result;
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
                    activity.getGroup() == null ? null : activity.getGroup().getId(),
                    activity.getTitle(),
                    activityWeeks
            ));
        }

        return rows;
    }

    private List<ActivityGroupDto> buildGroups(Long pageId, List<ActivityRowDto> rows) {
        List<ActivityGroup> groups = groupRepository.findByPageIdOrderBySortOrderAscIdAsc(pageId);

        List<ActivityGroupDto> result = new ArrayList<>();

        for (ActivityGroup group : groups) {
            List<ActivityRowDto> groupRows = rows.stream()
                    .filter(row -> group.getId().equals(row.groupId()))
                    .toList();

            result.add(new ActivityGroupDto(
                    group.getId(),
                    group.getTitle(),
                    groupRows
            ));
        }

        List<ActivityRowDto> withoutGroupRows = rows.stream()
                .filter(row -> row.groupId() == null)
                .toList();

        result.add(new ActivityGroupDto(
                null,
                "",
                withoutGroupRows
        ));

        return result;
    }

    private String key(Long activityId, LocalDate date) {
        return activityId + "_" + date;
    }
}
