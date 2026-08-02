package dev.egor.galochkiapp.month;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityGroup;
import dev.egor.galochkiapp.activity.ActivityGroupService;
import dev.egor.galochkiapp.activity.ActivityService;
import dev.egor.galochkiapp.galochka.Galochka;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MonthPageServiceIntegrationTest {

    @Autowired
    private MonthPageService monthPageService;

    @Autowired
    private GalochkiPageService pageService;

    @Autowired
    private ActivityGroupService groupService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private GalochkaRepository galochkaRepository;

    @Test
    @DisplayName("Месячная модель содержит страницу, дела и значения галочек на всех показанных днях")
    void buildsCompleteMonthPageModel() {
        GalochkiPage page = pageService.create("Апрель", DayOfWeek.MONDAY);
        Activity activity = activityService.create(page.getId(), "Зарядка", null);
        saveMark(activity, LocalDate.of(2026, 4, 10));

        MonthPageDto result = monthPageService.build(page.getId(), YearMonth.of(2026, 4));

        assertThat(result.pageId()).isEqualTo(page.getId());
        assertThat(result.pageTitle()).isEqualTo("Апрель");
        assertThat(result.previousMonth()).isEqualTo(YearMonth.of(2026, 3));
        assertThat(result.nextMonth()).isEqualTo(YearMonth.of(2026, 5));
        assertThat(result.rows()).singleElement().satisfies(row -> {
            assertThat(row.title()).isEqualTo("Зарядка");
            assertThat(row.weeks())
                    .flatMap(ActivityWeekCellsDto::cells)
                    .filteredOn(cell -> cell.date().equals("2026-04-10"))
                    .singleElement()
                    .extracting(GalochkaCellDto::value)
                    .isEqualTo("1");
        });
    }

    @Test
    @DisplayName("Группы в месячной модели следуют сохранённому sortOrder")
    void displaysGroupsInPersistedOrder() {
        GalochkiPage page = pageService.create("Страница", DayOfWeek.MONDAY);
        ActivityGroup first = groupService.create(page.getId(), "Первая");
        ActivityGroup second = groupService.create(page.getId(), "Вторая");
        activityService.create(page.getId(), "Дело 1", first.getId());
        activityService.create(page.getId(), "Дело 2", second.getId());
        groupService.reorderGroupsForCurrentOwner(page.getId(), List.of(second.getId(), first.getId()));

        MonthPageDto result = monthPageService.build(page.getId(), YearMonth.of(2026, 4));

        assertThat(result.groups())
                .extracting(ActivityGroupDto::title)
                .containsExactly("Вторая", "Первая");
    }

    @Test
    @DisplayName("Блок «Без группы» отображается после всех именованных групп")
    void displaysUngroupedActivitiesLast() {
        GalochkiPage page = pageService.create("Страница", DayOfWeek.MONDAY);
        ActivityGroup group = groupService.create(page.getId(), "Именованная группа");
        Activity grouped = activityService.create(page.getId(), "В группе", group.getId());
        Activity ungrouped = activityService.create(page.getId(), "Без группы", null);

        MonthPageDto result = monthPageService.build(page.getId(), YearMonth.of(2026, 4));

        assertThat(result.groups()).hasSize(2);
        assertThat(result.groups().get(0).groupId()).isEqualTo(group.getId());
        assertThat(result.groups().get(0).rows()).extracting(ActivityRowDto::activityId)
                .containsExactly(grouped.getId());
        assertThat(result.groups().get(1).groupId()).isNull();
        assertThat(result.groups().get(1).rows()).extracting(ActivityRowDto::activityId)
                .containsExactly(ungrouped.getId());
    }

    @Test
    @DisplayName("Апрель 2026 при начале недели в понедельник строится как пять полных недель")
    void buildsFullWeeksFromConfiguredWeekStart() {
        GalochkiPage page = pageService.create("Страница", DayOfWeek.MONDAY);

        MonthPageDto result = monthPageService.build(page.getId(), YearMonth.of(2026, 4));

        assertThat(result.weeks()).hasSize(5);
        assertThat(result.weeks().get(0).startDate()).isEqualTo(LocalDate.of(2026, 3, 30));
        assertThat(result.weeks().get(0).endDate()).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(result.weeks().get(4).startDate()).isEqualTo(LocalDate.of(2026, 4, 27));
        assertThat(result.weeks().get(4).endDate()).isEqualTo(LocalDate.of(2026, 5, 3));
        assertThat(result.weeks()).allSatisfy(week -> assertThat(week.days()).hasSize(7));
        assertThat(result.weeks()).flatMap(WeekDto::days).hasSize(35);
    }

    private void saveMark(Activity activity, LocalDate date) {
        Galochka mark = new Galochka();
        mark.setActivity(activity);
        mark.setDate(date);
        mark.setValue(BigDecimal.ONE);
        galochkaRepository.saveAndFlush(mark);
    }
}

