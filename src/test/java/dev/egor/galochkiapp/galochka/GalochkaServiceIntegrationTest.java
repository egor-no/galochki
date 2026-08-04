package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityService;
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
class GalochkaServiceIntegrationTest {
    private static final LocalDate DATE = LocalDate.of(2026, 4, 8);

    @Autowired private GalochkaService galochkaService;
    @Autowired private GalochkaRepository galochkaRepository;
    @Autowired private GalochkiPageService pageService;
    @Autowired private ActivityService activityService;
    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("increment создаёт отсутствующую галочку со значением 0.5")
    void incrementCreatesMarkWithHalfValue() {
        Activity activity = createActivity();
        galochkaService.increment(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "0.5");
    }

    @Test
    @DisplayName("increment увеличивает значение галочки с 0.5 до 1")
    void incrementChangesHalfToOne() {
        Activity activity = createActivity();
        saveMark(activity, new BigDecimal("0.5"));
        galochkaService.increment(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "1");
    }

    @Test
    @DisplayName("increment не ограничивает значение единицей и увеличивает 1 до 1.5")
    void incrementChangesOneToOneAndHalf() {
        Activity activity = createActivity();
        saveMark(activity, new BigDecimal("1"));
        galochkaService.increment(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "1.5");
    }

    @Test
    @DisplayName("reset устанавливает значение 1.5 в ноль")
    void resetChangesOneAndHalfToZero() {
        Activity activity = createActivity();
        saveMark(activity, new BigDecimal("1.5"));
        galochkaService.reset(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "0");
    }

    @Test
    @DisplayName("reset создаёт отсутствующую галочку с нулевым значением")
    void resetCreatesMissingMarkWithZeroValue() {
        Activity activity = createActivity();
        galochkaService.reset(activity.getId(), DATE);
        assertPersistedValue(activity.getId(), "0");
    }

    private Activity createActivity() {
        GalochkiPage page = pageService.create("Страница", DayOfWeek.MONDAY, new BigDecimal("5"));
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
