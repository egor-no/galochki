package dev.egor.galochkiapp.activity;

import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final GalochkiPageService pageService;

    public ActivityService(ActivityRepository activityRepository,
                           GalochkiPageService pageService) {
        this.activityRepository = activityRepository;
        this.pageService = pageService;
    }

    public List<Activity> getActiveActivitiesByPage(Long pageId) {
        return activityRepository.findByPageIdAndActiveTrueOrderById(pageId);
    }

    public Activity create(Long pageId, String title) {
        GalochkiPage page = pageService.getByIdForCurrentOwner(pageId);
        
        Activity activity = new Activity();
        activity.setPage(page);
        activity.setTitle(title);
        activity.setActive(true);
        activity.setSortOrder(0);

        return activityRepository.save(activity);
    }
}