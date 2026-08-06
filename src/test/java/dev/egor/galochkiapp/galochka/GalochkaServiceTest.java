package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.PageType;
import dev.egor.galochkiapp.week.PageWeekOverheadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GalochkaServiceTest {

    private static final long PAGE_ID = 11L;
    private static final long ACTIVITY_ID = 22L;
    private static final LocalDate DATE = LocalDate.of(2026, 4, 8);

    @Mock private GalochkaRepository galochkaRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private PageWeekOverheadService overheadService;
    @InjectMocks private GalochkaService galochkaService;

    @Test
    void leftClickRecalculatesOverheadForBinary() {
        stubExistingMark(PageType.BINARY);

        galochkaService.handleLeftClick(ACTIVITY_ID, DATE);

        verify(overheadService).recalculateFrom(PAGE_ID, DATE);
    }

    @Test
    void leftClickRecalculatesOverheadForHalfStep() {
        stubExistingMark(PageType.HALF_STEP);

        galochkaService.handleLeftClick(ACTIVITY_ID, DATE);

        verify(overheadService).recalculateFrom(PAGE_ID, DATE);
    }

    @Test
    void numericValueDoesNotRecalculateOverheadForNumber() {
        stubExistingMark(PageType.NUMBER);

        galochkaService.setNumericValue(ACTIVITY_ID, DATE, new BigDecimal("-2.5"));

        verify(overheadService, never()).recalculateFrom(PAGE_ID, DATE);
    }

    @Test
    void resetDoesNotRecalculateOverheadForNumber() {
        stubExistingMark(PageType.NUMBER);

        galochkaService.reset(ACTIVITY_ID, DATE);

        verify(overheadService, never()).recalculateFrom(PAGE_ID, DATE);
    }

    private void stubExistingMark(PageType pageType) {
        GalochkiPage page = mock(GalochkiPage.class);
        lenient().when(page.getPageType()).thenReturn(pageType);
        lenient().when(page.getId()).thenReturn(PAGE_ID);
        lenient().when(page.supportsWeeklyNorm()).thenReturn(pageType != PageType.NUMBER);
        Activity activity = new Activity();
        activity.setPage(page);
        Galochka mark = new Galochka();
        mark.setActivity(activity);
        mark.setDate(DATE);
        mark.setValue(BigDecimal.ZERO);

        when(galochkaRepository.findByActivityIdAndDate(ACTIVITY_ID, DATE)).thenReturn(Optional.of(mark));
        when(galochkaRepository.save(mark)).thenReturn(mark);
    }
}
