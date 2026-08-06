package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityService;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import dev.egor.galochkiapp.page.PageType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GalochkaServiceIntegrationTest {
    private static final LocalDate DATE = LocalDate.of(2026, 4, 8);

    @Autowired private GalochkaService galochkaService;
    @Autowired private GalochkaRepository galochkaRepository;
    @Autowired private GalochkiPageService pageService;
    @Autowired private ActivityService activityService;
    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("handleLeftClick создаёт отсутствующую галочку HALF_STEP со значением 0.5")
    void leftClickCreatesHalfStepMarkWithHalfValue() {
        Activity activity = createActivity(PageType.HALF_STEP);
        galochkaService.handleLeftClick(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "0.5");
    }

    @Test
    @DisplayName("handleLeftClick увеличивает HALF_STEP с 0.5 до 1")
    void leftClickChangesHalfToOne() {
        Activity activity = createActivity(PageType.HALF_STEP);
        saveMark(activity, new BigDecimal("0.5"));
        galochkaService.handleLeftClick(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "1");
    }

    @Test
    @DisplayName("handleLeftClick увеличивает HALF_STEP с 1 до 1.5")
    void leftClickChangesOneToOneAndHalf() {
        Activity activity = createActivity(PageType.HALF_STEP);
        saveMark(activity, new BigDecimal("1"));
        galochkaService.handleLeftClick(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "1.5");
    }

    @Test
    @DisplayName("handleLeftClick переключает BINARY 0 → 1 → 0")
    void leftClickTogglesBinaryValue() {
        Activity activity = createActivity(PageType.BINARY);

        galochkaService.handleLeftClick(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "1");
        galochkaService.handleLeftClick(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "0");
    }

    @Test
    @DisplayName("setNumericValue сохраняет дробные и отрицательные значения NUMBER")
    void setsFractionalAndNegativeNumericValues() {
        Activity activity = createActivity(PageType.NUMBER);

        galochkaService.setNumericValue(activity.getId(), DATE, new BigDecimal("2.75"));
        assertPersistedValue(activity.getId(), "2.75");
        galochkaService.setNumericValue(activity.getId(), DATE, new BigDecimal("-1.25"));
        assertPersistedValue(activity.getId(), "-1.25");
    }

    @Test
    @DisplayName("handleLeftClick запрещён для NUMBER")
    void rejectsLeftClickForNumberPage() {
        Activity activity = createActivity(PageType.NUMBER);

        assertThatThrownBy(() -> galochkaService.handleLeftClick(activity.getId(), DATE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("setNumericValue запрещён для BINARY и HALF_STEP")
    void rejectsNumericValueForNonNumberPages() {
        Activity binary = createActivity(PageType.BINARY);
        Activity halfStep = createActivity(PageType.HALF_STEP);

        assertThatThrownBy(() -> galochkaService.setNumericValue(binary.getId(), DATE, BigDecimal.ONE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> galochkaService.setNumericValue(halfStep.getId(), DATE, BigDecimal.ONE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("reset устанавливает значение 1.5 в ноль")
    void resetChangesOneAndHalfToZero() {
        Activity activity = createActivity(PageType.HALF_STEP);
        saveMark(activity, new BigDecimal("1.5"));
        galochkaService.reset(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "0");
    }

    @Test
    @DisplayName("reset создаёт отсутствующую галочку с нулевым значением")
    void resetCreatesMissingMarkWithZeroValue() {
        Activity activity = createActivity(PageType.HALF_STEP);
        galochkaService.reset(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "0");
    }

    @Test
    @DisplayName("reset устанавливает ноль для любого типа страницы")
    void resetSetsZeroForEveryPageType() {
        for (PageType pageType : PageType.values()) {
            Activity activity = createActivity(pageType);
            saveMark(activity, new BigDecimal("3.25"));

            galochkaService.reset(activity.getId(), DATE);

            assertPersistedValue(activity.getId(), "0");
        }
    }

    private Activity createActivity(PageType pageType) {
        GalochkiPage page = pageService.create("Страница", DayOfWeek.MONDAY, pageType, new BigDecimal("5"));
        return activityService.create(page.getId(), "Дело", null);
    }

    private void saveMark(Activity activity, BigDecimal value) {
        Galochka mark = new Galochka();
        mark.setActivity(activity);
        mark.setDate(DATE);
        mark.setValue(value);
        galochkaRepository.saveAndFlush(mark);
    }

    private void assertPersistedValue(Long activityId, String expected) {
        entityManager.flush();
        entityManager.clear();
        BigDecimal actual = galochkaRepository.findByActivityIdAndDate(activityId, DATE)
                .orElseThrow().getValue();
        assertThat(actual).isEqualByComparingTo(expected);
    }
}
