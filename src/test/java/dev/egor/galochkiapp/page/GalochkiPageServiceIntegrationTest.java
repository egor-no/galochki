package dev.egor.galochkiapp.page;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityGroup;
import dev.egor.galochkiapp.activity.ActivityGroupRepository;
import dev.egor.galochkiapp.activity.ActivityRepository;
import dev.egor.galochkiapp.galochka.Galochka;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.week.PageWeekOverhead;
import dev.egor.galochkiapp.week.PageWeekOverheadRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GalochkiPageServiceIntegrationTest {

    private static final LocalDate WEEK_START = LocalDate.of(2026, 4, 6);

    @Autowired private GalochkiPageService pageService;
    @Autowired private GalochkiPageRepository pageRepository;
    @Autowired private ActivityGroupRepository groupRepository;
    @Autowired private ActivityRepository activityRepository;
    @Autowired private GalochkaRepository galochkaRepository;
    @Autowired private PageWeekOverheadRepository overheadRepository;
    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("BINARY page without norm persists all display settings")
    void createsBinaryPageWithoutWeeklyNorm() {
        GalochkiPage created = pageService.create("Binary", DayOfWeek.SUNDAY, PageType.BINARY,
                BigDecimal.ZERO, true, false, true);

        GalochkiPage page = flushClearAndRead(created.getId());

        assertThat(page.getPageType()).isEqualTo(PageType.BINARY);
        assertThat(page.getWeeklyNorm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(page.getWeekStartDay()).isEqualTo(DayOfWeek.SUNDAY);
        assertThat(page.isShowStatisticsWithoutNorm()).isTrue();
        assertThat(page.isShowWeekCompletedCheck()).isFalse();
        assertThat(page.isShowWeekPercentage()).isTrue();
        assertThat(page.hasWeeklyNorm()).isFalse();
        assertThat(page.shouldShowStatistics()).isTrue();
    }

    @Test
    @DisplayName("HALF_STEP page with norm persists its settings and shows statistics")
    void createsHalfStepPageWithWeeklyNorm() {
        GalochkiPage created = pageService.create("Half step", DayOfWeek.MONDAY, PageType.HALF_STEP,
                new BigDecimal("7.5"), false, true, false);

        GalochkiPage page = flushClearAndRead(created.getId());

        assertThat(page.getPageType()).isEqualTo(PageType.HALF_STEP);
        assertThat(page.getWeeklyNorm()).isEqualByComparingTo("7.5");
        assertThat(page.isShowStatisticsWithoutNorm()).isFalse();
        assertThat(page.isShowWeekCompletedCheck()).isTrue();
        assertThat(page.isShowWeekPercentage()).isFalse();
        assertThat(page.hasWeeklyNorm()).isTrue();
        assertThat(page.shouldShowStatistics()).isTrue();
    }

    @Test
    @DisplayName("NUMBER page ignores norm and display settings")
    void createsNumberPageWithNormalizedSettings() {
        GalochkiPage created = pageService.create("Numbers", DayOfWeek.FRIDAY, PageType.NUMBER,
                new BigDecimal("17.5"), true, true, true);

        GalochkiPage page = flushClearAndRead(created.getId());

        assertThat(page.getPageType()).isEqualTo(PageType.NUMBER);
        assertThat(page.getWeeklyNorm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(page.isShowStatisticsWithoutNorm()).isFalse();
        assertThat(page.isShowWeekCompletedCheck()).isFalse();
        assertThat(page.isShowWeekPercentage()).isFalse();
        assertThat(page.hasWeeklyNorm()).isFalse();
    }

    @Test
    @DisplayName("Deleting a page removes ungrouped activity, marks and overhead")
    void deletesPageWithUngroupedActivity() {
        Graph graph = createGraph("Ungrouped", false, 1, true, true);

        deleteAndAssertGone(graph);
    }

    @Test
    @DisplayName("Deleting page removes group, grouped activity, marks and overhead through real FK")
    void deletesPageWithGroupedActivity() {
        Graph graph = createGraph("Grouped", true, 1, true, true);
        assertGraphExists(graph);

        deleteAndAssertGone(graph);
    }

    @Test
    @DisplayName("Deleting a page removes multiple groups and grouped and ungrouped activities")
    void deletesPageWithMultipleGroups() {
        Graph graph = createGraph("Many groups", true, 2, true, true);
        Activity ungrouped = saveActivity(graph.page(), null, "Ungrouped");
        Galochka mark = saveMark(ungrouped, WEEK_START.plusDays(3));
        graph = graph.with(ungrouped, mark);
        flushAndClear();
        assertGraphExists(graph);

        deleteAndAssertGone(graph);
    }

    @Test
    @DisplayName("Deleting one page does not touch another page graph")
    void deletingOnePageDoesNotAffectAnother() {
        Graph deleted = createGraph("First", true, 1, true, true);
        Graph retained = createGraph("Second", true, 1, true, true);
        flushAndClear();

        pageService.deleteForCurrentOwner(deleted.page().getId());
        flushAndClear();

        assertGraphGone(deleted);
        assertGraphExists(retained);
    }

    @Test
    @DisplayName("Deleting page with an empty group succeeds")
    void deletesPageWithEmptyGroup() {
        GalochkiPage page = pageService.create("Empty group", DayOfWeek.MONDAY, PageType.HALF_STEP,
                BigDecimal.ZERO, false, true, false);
        Graph graph = new Graph(page);
        graph.groups.add(saveGroup(page, "Empty"));
        flushAndClear();
        assertGraphExists(graph);

        deleteAndAssertGone(graph);
    }

    @Test
    @DisplayName("Deleting page with activity without marks succeeds")
    void deletesPageWithActivityWithoutMarks() {
        Graph graph = createGraph("No marks", false, 1, false, false);
        flushAndClear();
        assertGraphExists(graph);

        deleteAndAssertGone(graph);
    }

    private Graph createGraph(String title, boolean grouped, int groupCount,
                              boolean withMarks, boolean withOverhead) {
        GalochkiPage page = pageService.create(title, DayOfWeek.MONDAY, PageType.HALF_STEP,
                new BigDecimal("5"), false, true, true);
        Graph graph = new Graph(page);

        for (int i = 0; i < groupCount; i++) {
            ActivityGroup group = saveGroup(page, "Group " + i);
            graph.groups.add(group);
            if (grouped) {
                Activity activity = saveActivity(page, group, "Activity " + i);
                graph.activities.add(activity);
                if (withMarks) graph.marks.add(saveMark(activity, WEEK_START.plusDays(i)));
            }
        }
        if (!grouped && groupCount > 0) {
            Activity activity = saveActivity(page, null, "Activity");
            graph.activities.add(activity);
            if (withMarks) graph.marks.add(saveMark(activity, WEEK_START));
        }
        if (withOverhead) graph.overheads.add(saveOverhead(page));
        return graph;
    }

    private ActivityGroup saveGroup(GalochkiPage page, String title) {
        ActivityGroup group = new ActivityGroup();
        group.setPage(page);
        group.setTitle(title);
        return groupRepository.save(group);
    }

    private Activity saveActivity(GalochkiPage page, ActivityGroup group, String title) {
        Activity activity = new Activity();
        activity.setPage(page);
        activity.setGroup(group);
        activity.setTitle(title);
        return activityRepository.save(activity);
    }

    private Galochka saveMark(Activity activity, LocalDate date) {
        Galochka mark = new Galochka();
        mark.setActivity(activity);
        mark.setDate(date);
        mark.setValue(BigDecimal.ONE);
        return galochkaRepository.save(mark);
    }

    private PageWeekOverhead saveOverhead(GalochkiPage page) {
        PageWeekOverhead overhead = new PageWeekOverhead();
        overhead.setPage(page);
        overhead.setWeekStartDate(WEEK_START.plusWeeks(1));
        overhead.setValue(new BigDecimal("2"));
        return overheadRepository.save(overhead);
    }

    private void deleteAndAssertGone(Graph graph) {
        flushAndClear();
        pageService.deleteForCurrentOwner(graph.page().getId());
        flushAndClear();
        assertGraphGone(graph);
    }

    private void assertGraphExists(Graph graph) {
        assertThat(pageRepository.existsById(graph.page().getId())).isTrue();
        assertThat(graph.groups).allSatisfy(group -> assertThat(groupRepository.existsById(group.getId())).isTrue());
        assertThat(graph.activities).allSatisfy(activity -> assertThat(activityRepository.existsById(activity.getId())).isTrue());
        assertThat(graph.marks).allSatisfy(mark -> assertThat(galochkaRepository.existsById(mark.getId())).isTrue());
        assertThat(graph.overheads).allSatisfy(overhead -> assertThat(overheadRepository.existsById(overhead.getId())).isTrue());
    }

    private void assertGraphGone(Graph graph) {
        assertThat(pageRepository.existsById(graph.page().getId())).isFalse();
        assertThat(graph.groups).allSatisfy(group -> assertThat(groupRepository.existsById(group.getId())).isFalse());
        assertThat(graph.activities).allSatisfy(activity -> assertThat(activityRepository.existsById(activity.getId())).isFalse());
        assertThat(graph.marks).allSatisfy(mark -> assertThat(galochkaRepository.existsById(mark.getId())).isFalse());
        assertThat(graph.overheads).allSatisfy(overhead -> assertThat(overheadRepository.existsById(overhead.getId())).isFalse());
    }

    private GalochkiPage flushClearAndRead(Long pageId) {
        flushAndClear();
        return pageRepository.findById(pageId).orElseThrow();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static final class Graph {
        private final GalochkiPage page;
        private final List<ActivityGroup> groups = new java.util.ArrayList<>();
        private final List<Activity> activities = new java.util.ArrayList<>();
        private final List<Galochka> marks = new java.util.ArrayList<>();
        private final List<PageWeekOverhead> overheads = new java.util.ArrayList<>();

        private Graph(GalochkiPage page) { this.page = page; }
        private GalochkiPage page() { return page; }
        private Graph with(Activity activity, Galochka mark) {
            activities.add(activity);
            marks.add(mark);
            return this;
        }
    }
}
