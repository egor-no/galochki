package dev.egor.galochkiapp.page;

import dev.egor.galochkiapp.galochka.GalochkaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

@Service
public class GalochkiPageService {

    private static final Long TEMP_USER_ID = 1L;

    private final GalochkiPageRepository pageRepository;
    private final GalochkaRepository galochkaRepository;

    public GalochkiPageService(GalochkiPageRepository pageRepository,
                               GalochkaRepository galochkaRepository) {
        this.pageRepository = pageRepository;
        this.galochkaRepository = galochkaRepository;
    }

    public Long getCurrentOwnerId() {
        return TEMP_USER_ID;
    }

    public List<GalochkiPage> getAllPagesForCurrentOwner() {
        return pageRepository.findByOwnerIdOrderById(getCurrentOwnerId());
    }

    public GalochkiPage getByIdForCurrentOwner(Long id) {
        return pageRepository.findByIdAndOwnerId(id, getCurrentOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Страница не найдена: " + id));
    }

    @Transactional
    public void updateTitleForCurrentOwner(Long pageId, String title) {
        GalochkiPage page = getByIdForCurrentOwner(pageId);
        page.setTitle(title);
    }

    @Transactional
    public void deleteForCurrentOwner(Long pageId) {
        GalochkiPage page = getByIdForCurrentOwner(pageId);

        galochkaRepository.deleteByActivityPageId(pageId);

        pageRepository.delete(page);
    }

    @Transactional
    public GalochkiPage create(String title, DayOfWeek weekStartDay, BigDecimal weeklyNorm) {
        validateWeeklyNorm(weeklyNorm);

        GalochkiPage page = new GalochkiPage();
        page.setTitle(title);
        page.setWeekStartDay(weekStartDay);
        page.setWeeklyNorm(weeklyNorm);
        page.setOwnerId(getCurrentOwnerId());

        return pageRepository.save(page);
    }

    public boolean hasPagesForCurrentOwner() {
        return pageRepository.existsByOwnerId(getCurrentOwnerId());
    }

    public GalochkiPage getFirstPageForCurrentOwner() {
        return pageRepository.findFirstByOwnerIdOrderById(getCurrentOwnerId())
                .orElseThrow(() -> new IllegalStateException("Страниц пока нет"));
    }

    @Transactional
    public void updateForCurrentOwner(Long pageId, String title, BigDecimal weeklyNorm) {
        validateWeeklyNorm(weeklyNorm);

        GalochkiPage page = getByIdForCurrentOwner(pageId);
        page.setTitle(title);
        page.setWeeklyNorm(weeklyNorm);
    }

    private void validateWeeklyNorm(BigDecimal weeklyNorm) {
        if (weeklyNorm == null || weeklyNorm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Недельная норма не может быть отрицательной");
        }
    }
}