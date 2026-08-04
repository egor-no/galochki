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

    @Mock private PageWeekOverheadRepository overheadRepository;
    @Mock private GalochkaRepository galochkaRepository;
    @Mock private GalochkiPageService pageService;
    @InjectMocks private PageWeekOverheadService overheadService;

    @Test
    @DisplayName("При равенстве итога недельной норме исходящий оверхэд равен нулю")
    void returnsZeroWhenWeekTotalEqualsNorm() {
        assertThat(overheadService.calculateOutgoingOverhead(
                new BigDecimal("5"), BigDecimal.ZERO, new BigDecimal("5")))
                .isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Превышение недельной нормы переносится в следующую неделю")
    void carriesExcessToNextWeek() {
        assertThat(overheadService.calculateOutgoingOverhead(
                new BigDecimal("7"), BigDecimal.ZERO, new BigDecimal("5")))
                .isEqualByComparingTo("2");
    }

    @Test
    @DisplayName("Входящий оверхэд участвует в эффективном итоге недели")
    void includesIncomingOverheadInEffectiveTotal() {
        assertThat(overheadService.calculateOutgoingOverhead(
                new BigDecimal("4"), new BigDecimal("2"), new BigDecimal("5")))
                .isEqualByComparingTo("1");
    }

    @Test
    @DisplayName("Недобор не создаёт отрицательный исходящий оверхэд")
    void clampsOutgoingOverheadToZero() {
        assertThat(overheadService.calculateOutgoingOverhead(
                new BigDecimal("3"), new BigDecimal("1"), new BigDecimal("5")))
                .isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Расчёт оверхэда сохраняет дробные значения BigDecimal")
    void calculatesFractionalOutgoingOverhead() {
        assertThat(overheadService.calculateOutgoingOverhead(
                new BigDecimal("3.5"), new BigDecimal("0.5"), new BigDecimal("3.5")))
                .isEqualByComparingTo("0.5");
    }

    @Test
    @DisplayName("Расчёт отклоняет null и отрицательную норму")
    void rejectsInvalidOutgoingOverheadArguments() {
        assertThatThrownBy(() -> overheadService.calculateOutgoingOverhead(null, BigDecimal.ZERO, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> overheadService.calculateOutgoingOverhead(
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getWeekStart возвращает настроенное начало недели")
    void findsConfiguredWeekStart() {
        GalochkiPage page = new GalochkiPage();
        page.setWeekStartDay(DayOfWeek.WEDNESDAY);
        when(pageService.getByIdForCurrentOwner(42L)).thenReturn(page);

        LocalDate result = overheadService.getWeekStart(42L, LocalDate.of(2026, 4, 12));

        assertThat(result).isEqualTo(LocalDate.of(2026, 4, 8));
    }

    @Test
    @DisplayName("getWeekStart сохраняет дату, уже являющуюся началом недели")
    void keepsDateThatIsAlreadyWeekStart() {
        GalochkiPage page = new GalochkiPage();
        page.setWeekStartDay(DayOfWeek.MONDAY);
        when(pageService.getByIdForCurrentOwner(42L)).thenReturn(page);
        LocalDate date = LocalDate.of(2026, 4, 6);

        assertThat(overheadService.getWeekStart(42L, date)).isEqualTo(date);
    }
}
