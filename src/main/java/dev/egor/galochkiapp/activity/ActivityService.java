package dev.egor.galochkiapp.activity;

import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final GalochkiPageService pageService;
    private final GalochkaRepository galochkaRepository;

    public ActivityService(ActivityRepository activityRepository,
                           GalochkiPageService pageService,
                           GalochkaRepository galochkaRepository) {
        this.activityRepository = activityRepository;
        this.pageService = pageService;
        this.galochkaRepository = galochkaRepository;
    }

    public List<Activity> getActiveActivitiesByPageForCurrentOwner(Long pageId) {
        pageService.getByIdForCurrentOwner(pageId);
        return activityRepository.findByPageIdAndActiveTrueOrderBySortOrderAscIdAsc(pageId);
    }

    @Transactional
    public Activity create(Long pageId, String title) {
        GalochkiPage page = pageService.getByIdForCurrentOwner(pageId);

        Activity activity = new Activity();
        activity.setPage(page);
        activity.setTitle(title);
        activity.setActive(true);
        activity.setSortOrder(activityRepository.countByPageIdAndActiveTrue(pageId));

        return activityRepository.save(activity);
    }

    @Transactional
    public void renameForCurrentOwner(Long activityId, String title) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Дело не найдено: " + activityId));

        pageService.getByIdForCurrentOwner(activity.getPage().getId());

        activity.setTitle(title);
    }

    @Transactional
    public void reorderForCurrentOwner(Long pageId, List<Long> activityIds) {
        pageService.getByIdForCurrentOwner(pageId);

        List<Activity> activities = activityRepository.findByPageIdAndActiveTrueOrderBySortOrderAscIdAsc(pageId);

        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        for (int i = 0; i < activityIds.size(); i++) {
            Activity activity = activityMap.get(activityIds.get(i));

            if (activity == null) {
                throw new IllegalArgumentException("Дело не найдено на этой странице: " + activityIds.get(i));
            }

            activity.setSortOrder(i);
        }
    }

    @Transactional
    public void deleteForCurrentOwner(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Дело не найдено: " + activityId));

        pageService.getByIdForCurrentOwner(activity.getPage().getId());

        galochkaRepository.deleteByActivityId(activityId);
        activityRepository.delete(activity);
    }
}