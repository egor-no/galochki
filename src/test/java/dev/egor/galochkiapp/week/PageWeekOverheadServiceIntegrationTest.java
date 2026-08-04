package dev.egor.galochkiapp.week;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityService;
import dev.egor.galochkiapp.galochka.Galochka;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.galochka.GalochkaService;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PageWeekOverheadServiceIntegrationTest {

    private static final LocalDate WEEK_START = LocalDate.of(2026, 4, 6);

    @Autowired
    private PageWeekOverheadService overheadService;

    @Autowired
    private PageWeekOverheadRepository overheadRepository;

    @Autowired
    private GalochkiPageService pageService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private GalochkaService galochkaService;

    @Autowired
    private GalochkaRepository galochkaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("calculateWeekTotal суммирует все галочки страницы за семь дней")
    void calculatesWeekTotal() {
        GalochkiPage page = createPage(new BigDecimal("10"));
        Activity first = activityService.create(page.getId(), "Первое", null);
        Activity second = activityService.create(page.getId(), "Второе", null);
        saveMark(first, WEEK_START, new BigDecimal("2.5"));
        saveMark(second, WEEK_START.plusDays(3), new BigDecimal("4"));
        saveMark(first, WEEK_START.plusDays(7), new BigDecimal("100"));

        BigDecimal total = overheadService.calculateWeekTotal(page.getId(), WEEK_START);

        assertThat(total).isEqualByComparingTo("6.5");
    }

    @Test
    @DisplayName("calculateOutgoingOverhead возвращает ноль, когда сумма с входящим остатком не превышает норму")
    void returnsZeroWhenWeekDoesNotExceedNorm() {
        BigDecimal result = overheadService.calculateOutgoingOverhead(
                new BigDecimal("7"), new BigDecimal("2"), new BigDecimal("10"));

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateOutgoingOverhead переносит превышение нормы на следующую неделю")
    void calculatesOverheadWhenWeekExceedsNorm() {
        BigDecimal result = overheadService.calculateOutgoingOverhead(
                new BigDecimal("12"), new BigDecimal("3"), new BigDecimal("10"));

        assertThat(result).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("saveOrDeleteOverhead создаёт, обновляет и удаляет запись оверхэда")
    void savesUpdatesAndDeletesOverhead() {
        GalochkiPage page = createPage(new BigDecimal("10"));

        assertThat(overheadService.saveOrDeleteOverhead(page.getId(), WEEK_START, new BigDecimal("2"))).isTrue();
        assertThat(overheadRepository.findByPageIdAndWeekStartDate(page.getId(), WEEK_START)
                .orElseThrow().getValue()).isEqualByComparingTo("2");

        assertThat(overheadService.saveOrDeleteOverhead(page.getId(), WEEK_START, new BigDecimal("4"))).isTrue();
        entityManager.flush();
        assertThat(overheadRepository.findByPageIdAndWeekStartDate(page.getId(), WEEK_START)
                .orElseThrow().getValue()).isEqualByComparingTo("4");

        assertThat(overheadService.saveOrDeleteOverhead(page.getId(), WEEK_START, BigDecimal.ZERO)).isTrue();
        entityManager.flush();
        assertThat(overheadRepository.findByPageIdAndWeekStartDate(page.getId(), WEEK_START)).isEmpty();
    }

    @Test
    @DisplayName("recalculateFrom сохраняет исходящий оверхэд под началом следующей недели")
    void recalculatesOverheadFromChangedWeek() {
        GalochkiPage page = createPage(new BigDecimal("10"));
        Activity activity = activityService.create(page.getId(), "Дело", null);
        saveMark(activity, WEEK_START.plusDays(1), new BigDecimal("13"));

        overheadService.recalculateFrom(page.getId(), WEEK_START.plusDays(1));
        entityManager.flush();

        assertThat(overheadRepository.findByPageIdAndWeekStartDate(page.getId(), WEEK_START.plusWeeks(1))
                .orElseThrow().getValue()).isEqualByComparingTo("3");
    }

    @Test
    @DisplayName("Изменение галочки через GalochkaService создаёт и удаляет оверхэд")
    void incrementAndResetChangeOverhead() {
        GalochkiPage page = createPage(BigDecimal.ONE);
        Activity first = activityService.create(page.getId(), "Первое", null);
        Activity second = activityService.create(page.getId(), "Второе", null);
        LocalDate date = WEEK_START.plusDays(2);

        galochkaService.increment(first.getId(), date);
        assertThat(overheadRepository.findByPageIdAndWeekStartDate(page.getId(), WEEK_START.plusWeeks(1))).isEmpty();

        galochkaService.increment(first.getId(), date);
        galochkaService.increment(second.getId(), date);
        assertThat(overheadRepository.findByPageIdAndWeekStartDate(page.getId(), WEEK_START.plusWeeks(1))
                .orElseThrow().getValue()).isEqualByComparingTo("0.5");

        galochkaService.reset(second.getId(), date);
        assertThat(overheadRepository.findByPageIdAndWeekStartDate(page.getId(), WEEK_START.plusWeeks(1))).isEmpty();
    }

    @Test
    @DisplayName("recalculateFrom удаляет все оверхэды страницы при отключённой норме")
    void disabledNormDeletesExistingOverheads() {
        GalochkiPage page = createPage(new BigDecimal("10"));
        overheadService.saveOrDeleteOverhead(page.getId(), WEEK_START, new BigDecimal("2"));
        overheadService.saveOrDeleteOverhead(page.getId(), WEEK_START.plusWeeks(1), new BigDecimal("3"));
        pageService.updateForCurrentOwner(page.getId(), page.getTitle(), BigDecimal.ZERO);

        overheadService.recalculateFrom(page.getId(), WEEK_START.plusDays(1));
        entityManager.flush();

        assertThat(overheadRepository.findByPageIdOrderByWeekStartDateAsc(page.getId())).isEmpty();
    }

    @Test
    @DisplayName("Цепочка из трёх недель переносит оверхэд до достижения нормы")
    void recalculatesThreeWeekOverheadChain() {
        GalochkiPage page = createPage(new BigDecimal("5"));
        Activity activity = activityService.create(page.getId(), "Дело", null);
        saveMark(activity, WEEK_START, new BigDecimal("7"));
        saveMark(activity, WEEK_START.plusWeeks(1), new BigDecimal("4"));
        saveMark(activity, WEEK_START.plusWeeks(2), new BigDecimal("4"));

        overheadService.recalculateFrom(page.getId(), WEEK_START);
        entityManager.flush();
        entityManager.clear();

        assertThat(readOverhead(page.getId(), WEEK_START.plusWeeks(1))).isEqualByComparingTo("2");
        assertThat(readOverhead(page.getId(), WEEK_START.plusWeeks(2))).isEqualByComparingTo("1");
        assertThat(overheadRepository.findByPageIdAndWeekStartDate(page.getId(), WEEK_START.plusWeeks(3))).isEmpty();
    }

    @Test
    @DisplayName("increment в старой неделе увеличивает все зависимые оверхэды")
    void incrementInOldWeekIncreasesFollowingOverheads() {
        GalochkiPage page = createPage(new BigDecimal("5"));
        Activity activity = activityService.create(page.getId(), "Дело", null);
        saveMark(activity, WEEK_START, new BigDecimal("7"));
        saveMark(activity, WEEK_START.plusWeeks(1), new BigDecimal("4"));
        saveMark(activity, WEEK_START.plusWeeks(2), new BigDecimal("4"));
        overheadService.recalculateFrom(page.getId(), WEEK_START);

        galochkaService.increment(activity.getId(), WEEK_START);
        entityManager.flush();
        entityManager.clear();

        assertThat(readOverhead(page.getId(), WEEK_START.plusWeeks(1))).isEqualByComparingTo("2.5");
        assertThat(readOverhead(page.getId(), WEEK_START.plusWeeks(2))).isEqualByComparingTo("1.5");
        assertThat(readOverhead(page.getId(), WEEK_START.plusWeeks(3))).isEqualByComparingTo("0.5");
    }

    @Test
    @DisplayName("reset в старой неделе очищает устаревшие зависимые оверхэды")
    void resetInOldWeekClearsFollowingOverheads() {
        GalochkiPage page = createPage(new BigDecimal("5"));
        Activity activity = activityService.create(page.getId(), "Дело", null);
        saveMark(activity, WEEK_START, new BigDecimal("7"));
        saveMark(activity, WEEK_START.plusWeeks(1), new BigDecimal("4"));
        saveMark(activity, WEEK_START.plusWeeks(2), new BigDecimal("4"));
        overheadService.recalculateFrom(page.getId(), WEEK_START);

        galochkaService.reset(activity.getId(), WEEK_START);
        entityManager.flush();
        entityManager.clear();

        assertThat(galochkaRepository.findByActivityIdAndDate(activity.getId(), WEEK_START)
                .orElseThrow().getValue()).isEqualByComparingTo("0");
        assertThat(overheadRepository.findByPageIdOrderByWeekStartDateAsc(page.getId())).isEmpty();
    }

    private GalochkiPage createPage(BigDecimal weeklyNorm) {
        return pageService.create("Страница", DayOfWeek.MONDAY, weeklyNorm);
    }

    private void saveMark(Activity activity, LocalDate date, BigDecimal value) {
        Galochka mark = new Galochka();
        mark.setActivity(activity);
        mark.setDate(date);
        mark.setValue(value);
        galochkaRepository.saveAndFlush(mark);
    }

    private BigDecimal readOverhead(Long pageId, LocalDate weekStart) {
        return overheadRepository.findByPageIdAndWeekStartDate(pageId, weekStart)
                .orElseThrow().getValue();
    }
}
