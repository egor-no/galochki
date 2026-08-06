package dev.egor.galochkiapp.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GalochkiPageServiceIntegrationTest {

    @Autowired
    private GalochkiPageService pageService;

    @Test
    @DisplayName("NUMBER всегда создаётся с нулевой недельной нормой")
    void createsNumberPageWithZeroWeeklyNorm() {
        GalochkiPage page = pageService.create(
                "Числа", DayOfWeek.MONDAY, PageType.NUMBER, new BigDecimal("17.5"));

        assertThat(page.getPageType()).isEqualTo(PageType.NUMBER);
        assertThat(page.getWeeklyNorm()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("BINARY и HALF_STEP сохраняют переданную недельную норму")
    void createsNormSupportingPagesWithProvidedWeeklyNorm() {
        GalochkiPage binary = pageService.create(
                "Бинарная", DayOfWeek.MONDAY, PageType.BINARY, new BigDecimal("4"));
        GalochkiPage halfStep = pageService.create(
                "Дробная", DayOfWeek.MONDAY, PageType.HALF_STEP, new BigDecimal("7.5"));

        assertThat(binary.getWeeklyNorm()).isEqualByComparingTo("4");
        assertThat(halfStep.getWeeklyNorm()).isEqualByComparingTo("7.5");
    }
}
