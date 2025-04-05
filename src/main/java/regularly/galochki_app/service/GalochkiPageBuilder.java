package regularly.galochki_app.service;

import org.springframework.stereotype.Service;
import regularly.galochki_app.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GalochkiPageBuilder {
    private final ItogService itogService;

    public GalochkiPageBuilder(ItogService itogService) {
        this.itogService = itogService;
    }

    public GalochkiPage build(Page page, List<Activity> activities, List<GalochkiWeek> weeks, YearMonth month) {
        List<GalochkiWeek> completeWeeks = fillMissingDays(weeks, activities.size(), month);

        GalochkiPage result = new GalochkiPage();

        result.setPage(page);
        result.setActivities(activities);
        result.setGalochki(completeWeeks);

        itogService.calculateDailyItog(result);

        return result;
    }

    private List<GalochkiWeek> fillMissingDays(
            List<GalochkiWeek> existingWeeks,
            int activityCount,
            YearMonth yearMonth
    ) {
        Map<LocalDate, GalochkiDay> dayMap = new HashMap<>();
        for (GalochkiWeek week : existingWeeks) {
            for (GalochkiDay day : week.getGalochkiDays()) {
                dayMap.put(day.getDay(), day);
            }
        }

        List<GalochkiWeek> result = new ArrayList<>();

        LocalDate cursor = yearMonth.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = yearMonth.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        while (!cursor.isAfter(end)) {
            List<GalochkiDay> weekDays = new ArrayList<>();
            int daysFromThisMonth = 0;

            for (int i = 0; i < 7; i++) {
                LocalDate date = cursor.plusDays(i);

                GalochkiDay day = dayMap.get(date);
                if (day == null) {
                    day = createEmptyDay(date, activityCount);
                }

                if (date.getMonth() == yearMonth.getMonth()) {
                    daysFromThisMonth++;
                }

                weekDays.add(day);
            }

            if (daysFromThisMonth >= 4) {
                GalochkiWeek week = new GalochkiWeek();
                week.setGalochkiDays(weekDays);
                week.setDailyItog(null);
                week.setOverflow(0.0);
                result.add(week);
            }

            cursor = cursor.plusWeeks(1);
        }

        return result;
    }

    private GalochkiDay createEmptyDay(LocalDate date, int activityCount) {
        GalochkiDay day = new GalochkiDay();
        day.setDay(date);

        List<Galochka> galochki = new ArrayList<>();
        for (int i = 0; i < activityCount; i++) {
            Galochka g = new Galochka();
            g.setValue(0.0);
            galochki.add(g);
        }

        day.setGalochki(galochki);
        return day;
    }

}
