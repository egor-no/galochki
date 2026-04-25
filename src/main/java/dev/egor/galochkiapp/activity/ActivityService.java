package dev.egor.galochkiapp.activity;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<Activity> getActiveActivities() {
        return activityRepository.findByActiveTrueOrderBySortOrderAscIdAsc();
    }

    public Activity create(String title) {
        Activity activity = new Activity();
        activity.setTitle(title);
        activity.setActive(true);
        activity.setSortOrder(0);

        return activityRepository.save(activity);
    }
}