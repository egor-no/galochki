package dev.egor.galochkiapp.week;

import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageWeekOverheadServiceTest {

    @Mock
    private PageWeekOverheadRepository overheadRepository;

    @Mock
    private GalochkaRepository galochkaRepository;

    @Mock
    private GalochkiPageService pageService;

    @InjectMocks
    private PageWeekOverheadService overheadService;

    @Test
    @DisplayName("Исходящий оверхэд равен положительной части выражения total + incoming - norm")
    void calculatesPositiveOutgoingOverhead() {
        assertThat(overheadService.calculateOutgoingOverhead(
                new BigDecimal("8.5"), new BigDecimal("4"), new BigDecimal("10")))
                .isEqualByComparingTo("2.5");
    }

    @Test
    @DisplayName("Исходящий оверхэд не становится отрицательным")
    void clampsOutgoingOverheadToZero() {
        assertThat(overheadService.calculateOutgoingOverhead(
                new BigDecimal("3"), BigDecimal.ONE, new BigDecimal("10")))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Расчёт оверхэда отклоняет null и отрицательную норму")
    void rejectsInvalidOutgoingOverheadArguments() {
        assertThatThrownBy(() -> overheadService.calculateOutgoingOverhead(null, BigDecimal.ZERO, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> overheadService.calculateOutgoingOverhead(
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getWeekStart возвращает начало недели согласно настройке страницы")
    void findsConfiguredWeekStart() {
        GalochkiPage page = new GalochkiPage();
        page.setWeekStartDay(DayOfWeek.WEDNESDAY);
        when(pageService.getByIdForCurrentOwner(42L)).thenReturn(page);

        LocalDate result = overheadService.getWeekStart(42L, LocalDate.of(2026, 4, 12));

        assertThat(result).isEqualTo(LocalDate.of(2026, 4, 8));
    }

    @Test
    @DisplayName("getWeekStart оставляет дату без изменений, если она уже является началом недели")
    void keepsDateThatIsAlreadyWeekStart() {
        GalochkiPage page = new GalochkiPage();
        page.setWeekStartDay(DayOfWeek.MONDAY);
        when(pageService.getByIdForCurrentOwner(42L)).thenReturn(page);

        LocalDate date = LocalDate.of(2026, 4, 6);

        assertThat(overheadService.getWeekStart(42L, date)).isEqualTo(date);
    }
}
