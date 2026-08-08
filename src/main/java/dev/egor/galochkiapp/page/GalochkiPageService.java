package dev.egor.galochkiapp.page;

import dev.egor.galochkiapp.activity.ActivityGroupRepository;
import dev.egor.galochkiapp.activity.ActivityRepository;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dev.egor.galochkiapp.week.PageWeekOverheadRepository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

@Service
public class GalochkiPageService {

    private static final Long TEMP_USER_ID = 1L;

    private final GalochkiPageRepository pageRepository;
    private final GalochkaRepository galochkaRepository;
    private final PageWeekOverheadRepository overheadRepository;
    private final ActivityGroupRepository groupRepository;
    private final ActivityRepository activityRepository;

    public GalochkiPageService(GalochkiPageRepository pageRepository,
                               GalochkaRepository galochkaRepository,
                               PageWeekOverheadRepository overheadRepository,
                               ActivityRepository activityRepository,
                               ActivityGroupRepository groupRepository) {
        this.pageRepository = pageRepository;
        this.galochkaRepository = galochkaRepository;
        this.overheadRepository = overheadRepository;
        this.activityRepository = activityRepository;
        this.groupRepository = groupRepository;
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
    public void updateDisplaySettingsForCurrentOwner(Long pageId, String title, boolean showStatisticsWithoutNorm, boolean showWeekCompletedCheck, boolean showWeekPercentage) {
        GalochkiPage page = getByIdForCurrentOwner(pageId);
        page.setTitle(title);
        if (page.getPageType() == PageType.NUMBER) {
            page.setShowStatisticsWithoutNorm(false);
            page.setShowWeekCompletedCheck(false);
            page.setShowWeekPercentage(false);
            return;
        }
        page.setShowStatisticsWithoutNorm(showStatisticsWithoutNorm);
        page.setShowWeekCompletedCheck(showWeekCompletedCheck);
        page.setShowWeekPercentage(showWeekPercentage);
    }

    @Transactional
    public void deleteForCurrentOwner(Long pageId) {
        GalochkiPage page = getByIdForCurrentOwner(pageId);

        galochkaRepository.deleteByActivityPageId(pageId);
        activityRepository.deleteByPageId(pageId);
        groupRepository.deleteByPageId(pageId);
        overheadRepository.deleteByPageId(pageId);

        pageRepository.delete(page);
    }

    @Transactional
    public GalochkiPage create(String title, DayOfWeek weekStartDay, PageType pageType, BigDecimal weeklyNorm, boolean showStatisticsWithoutNorm, boolean showWeekCompletedCheck, boolean showWeekPercentage) {
        if (pageType == null) {
            throw new IllegalArgumentException(
                    "Необходимо выбрать тип страницы"
            );
        }

        BigDecimal normalizedWeeklyNorm;

        if (pageType == PageType.NUMBER) {
            normalizedWeeklyNorm = BigDecimal.ZERO;
        } else {
            normalizedWeeklyNorm = weeklyNorm == null ? BigDecimal.ZERO : weeklyNorm;
            validateWeeklyNorm(normalizedWeeklyNorm);
        }

        GalochkiPage page = new GalochkiPage();
        page.setTitle(title);
        page.setWeekStartDay(weekStartDay);
        page.setPageType(pageType);
        page.setWeeklyNorm(normalizedWeeklyNorm);
        if (pageType == PageType.NUMBER) {
            page.setShowStatisticsWithoutNorm(false);
            page.setShowWeekCompletedCheck(false);
            page.setShowWeekPercentage(false);
        } else {
            page.setShowStatisticsWithoutNorm(showStatisticsWithoutNorm);
            page.setShowWeekCompletedCheck(showWeekCompletedCheck);
            page.setShowWeekPercentage(showWeekPercentage);
        }
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
        GalochkiPage page = getByIdForCurrentOwner(pageId);
        page.setTitle(title);

        if (page.supportsWeeklyNorm()) {
            BigDecimal normalizedWeeklyNorm = weeklyNorm == null ? BigDecimal.ZERO : weeklyNorm;
            validateWeeklyNorm(normalizedWeeklyNorm);
            page.setWeeklyNorm(normalizedWeeklyNorm);
        } else {
            page.setWeeklyNorm(BigDecimal.ZERO);
        }
    }

    private void validateWeeklyNorm(BigDecimal weeklyNorm) {
        if (weeklyNorm == null || weeklyNorm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Недельная норма не может быть отрицательной");
        }
    }
}