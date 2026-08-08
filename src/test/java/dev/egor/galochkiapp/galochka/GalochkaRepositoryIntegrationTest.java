package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityService;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import dev.egor.galochkiapp.page.PageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GalochkaRepositoryIntegrationTest {

    @Autowired
    private GalochkaRepository galochkaRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private GalochkiPageService pageService;

    @Test
    @DisplayName("База данных запрещает две галочки одного дела на одну дату")
    void rejectsDuplicateActivityAndDate() {
        GalochkiPage page = pageService.create("Страница", DayOfWeek.MONDAY, PageType.HALF_STEP, new BigDecimal("10"),
                false, true, false);
        Activity activity = activityService.create(page.getId(), "Дело", null);
        LocalDate date = LocalDate.of(2026, 4, 10);
        galochkaRepository.saveAndFlush(mark(activity, date));

        assertThatThrownBy(() -> galochkaRepository.saveAndFlush(mark(activity, date)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Galochka mark(Activity activity, LocalDate date) {
        Galochka mark = new Galochka();
        mark.setActivity(activity);
        mark.setDate(date);
        mark.setValue(BigDecimal.ONE);
        return mark;
    }
}
