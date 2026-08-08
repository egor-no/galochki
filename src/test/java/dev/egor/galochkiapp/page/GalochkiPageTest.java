package dev.egor.galochkiapp.page;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GalochkiPageTest {

    @Test
    void hasWeeklyNormIsFalseWhenNormIsZero() {
        GalochkiPage page = page(PageType.BINARY, BigDecimal.ZERO, false);

        assertThat(page.hasWeeklyNorm()).isFalse();
    }

    @Test
    void hasWeeklyNormIsTrueForBinaryAndHalfStepWithPositiveNorm() {
        assertThat(page(PageType.BINARY, BigDecimal.ONE, false).hasWeeklyNorm()).isTrue();
        assertThat(page(PageType.HALF_STEP, new BigDecimal("0.5"), false).hasWeeklyNorm()).isTrue();
    }

    @Test
    void hasWeeklyNormIsFalseForNumberEvenWithPositiveNorm() {
        GalochkiPage page = page(PageType.NUMBER, BigDecimal.TEN, false);

        assertThat(page.hasWeeklyNorm()).isFalse();
    }

    @Test
    void shouldNotShowStatisticsWithoutNormOrExplicitSetting() {
        GalochkiPage page = page(PageType.BINARY, BigDecimal.ZERO, false);

        assertThat(page.shouldShowStatistics()).isFalse();
    }

    @Test
    void shouldShowStatisticsWithoutNormWhenExplicitlyEnabled() {
        GalochkiPage page = page(PageType.BINARY, BigDecimal.ZERO, true);

        assertThat(page.shouldShowStatistics()).isTrue();
    }

    @Test
    void shouldShowStatisticsWithNormRegardlessOfExplicitSetting() {
        GalochkiPage page = page(PageType.HALF_STEP, BigDecimal.TEN, false);

        assertThat(page.shouldShowStatistics()).isTrue();
    }

    private GalochkiPage page(PageType pageType, BigDecimal weeklyNorm, boolean showStatisticsWithoutNorm) {
        GalochkiPage page = new GalochkiPage();
        page.setPageType(pageType);
        page.setWeeklyNorm(weeklyNorm);
        page.setShowStatisticsWithoutNorm(showStatisticsWithoutNorm);
        return page;
    }
}
