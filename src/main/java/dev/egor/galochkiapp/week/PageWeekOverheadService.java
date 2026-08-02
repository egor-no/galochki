package dev.egor.galochkiapp.week;

import dev.egor.galochkiapp.galochka.Galochka;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class PageWeekOverheadService {

    private final PageWeekOverheadRepository overheadRepository;
    private final GalochkaRepository galochkaRepository;
    private final GalochkiPageService pageService;

    public PageWeekOverheadService(PageWeekOverheadRepository overheadRepository, GalochkaRepository galochkaRepository, GalochkiPageService pageService) {
        this.overheadRepository = overheadRepository;
        this.galochkaRepository = galochkaRepository;
        this.pageService = pageService;
    }

    @Transactional(readOnly = true)
    public BigDecimal getIncomingOverhead(Long pageId, LocalDate weekStartDate) {
        pageService.getByIdForCurrentOwner(pageId);

        return overheadRepository.findByPageIdAndWeekStartDate(pageId, weekStartDate)
                .map(PageWeekOverhead::getValue)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateWeekTotal(Long pageId, LocalDate weekStartDate) {
        pageService.getByIdForCurrentOwner(pageId);

        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<Galochka> galochki = galochkaRepository.findByActivityPageIdAndDateBetween(pageId, weekStartDate, weekEndDate);

        return galochki.stream()
                .map(Galochka::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateOutgoingOverhead(BigDecimal weekTotal, BigDecimal incomingOverhead, BigDecimal weeklyNorm) {
        if (weekTotal == null || incomingOverhead == null || weeklyNorm == null) {
            throw new IllegalArgumentException("Данные для расчёта оверхэда не могут быть null");
        }

        if (weeklyNorm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Недельная норма не может быть отрицательной");
        }

        BigDecimal overhead = weekTotal.add(incomingOverhead).subtract(weeklyNorm);
        return overhead.max(BigDecimal.ZERO);
    }

    @Transactional
    public boolean saveOrDeleteOverhead(Long pageId, LocalDate weekStartDate, BigDecimal value) {
        GalochkiPage page = pageService.getByIdForCurrentOwner(pageId);

        PageWeekOverhead existing = overheadRepository.findByPageIdAndWeekStartDate(pageId, weekStartDate).orElse(null);

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            if (existing == null) {
                return false;
            }

            overheadRepository.delete(existing);
            return true;
        }

        if (existing != null) {
            if (existing.getValue().compareTo(value) == 0) {
                return false;
            }

            existing.setValue(value);
            return true;
        }

        PageWeekOverhead overhead = new PageWeekOverhead();
        overhead.setPage(page);
        overhead.setWeekStartDate(weekStartDate);
        overhead.setValue(value);
        overheadRepository.save(overhead);

        return true;
    }

    @Transactional(readOnly = true)
    public LocalDate getWeekStart(Long pageId, LocalDate date) {
        GalochkiPage page = pageService.getByIdForCurrentOwner(pageId);
        DayOfWeek weekStartDay = page.getWeekStartDay();

        LocalDate result = date;

        while (result.getDayOfWeek() != weekStartDay) {
            result = result.minusDays(1);
        }

        return result;
    }

    @Transactional
    public void recalculateFrom(Long pageId, LocalDate changedDate) {
        GalochkiPage page = pageService.getByIdForCurrentOwner(pageId);
        LocalDate weekStart = getWeekStart(pageId, changedDate);

        if (page.getWeeklyNorm().compareTo(BigDecimal.ZERO) == 0) {
            overheadRepository.deleteByPageId(pageId);
            return;
        }

        while (true) {
            BigDecimal incomingOverhead = getIncomingOverhead(pageId, weekStart);
            BigDecimal weekTotal = calculateWeekTotal(pageId, weekStart);
            BigDecimal outgoingOverhead = calculateOutgoingOverhead(weekTotal, incomingOverhead, page.getWeeklyNorm());

            LocalDate nextWeekStart = weekStart.plusWeeks(1);
            boolean changed = saveOrDeleteOverhead(pageId, nextWeekStart, outgoingOverhead);

            if (!changed) {
                break;
            }

            weekStart = nextWeekStart;
        }
    }
}