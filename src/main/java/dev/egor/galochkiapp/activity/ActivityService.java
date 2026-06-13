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
    private final ActivityGroupRepository activityGroupRepository;

    public ActivityService(ActivityRepository activityRepository,
                           GalochkiPageService pageService,
                           GalochkaRepository galochkaRepository,
                           ActivityGroupRepository activityGroupRepository) {
        this.activityRepository = activityRepository;
        this.pageService = pageService;
        this.galochkaRepository = galochkaRepository;
        this.activityGroupRepository = activityGroupRepository;
    }

    public List<Activity> getActiveActivitiesByPageForCurrentOwner(Long pageId) {
        pageService.getByIdForCurrentOwner(pageId);
        return activityRepository.findByPageIdAndActiveTrueOrderByGroupSortOrderAscSortOrderAscIdAsc(pageId);
    }

    @Transactional
    public Activity create(Long pageId, String title, Long groupId) {
        GalochkiPage page = pageService.getByIdForCurrentOwner(pageId);

        ActivityGroup group = null;

        if (groupId != null) {
            group = activityGroupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + groupId));

            if (!group.getPage().getId().equals(pageId)) {
                throw new IllegalArgumentException("Группа не принадлежит этой странице");
            }
        }

        int sortOrder;

        if (groupId == null) {
            sortOrder = activityRepository
                    .findByPageIdAndGroupIsNullAndActiveTrueOrderBySortOrderAscIdAsc(pageId)
                    .size();
        } else {
            sortOrder = activityRepository
                    .countByPageIdAndGroupIdAndActiveTrue(pageId, groupId);
        }

        Activity activity = new Activity();
        activity.setPage(page);
        activity.setTitle(title);
        activity.setActive(true);
        activity.setGroup(group);
        activity.setSortOrder(sortOrder);

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
    public void reorderForCurrentOwner(Long pageId, List<ActivityReorderGroupDto> groups) {
        pageService.getByIdForCurrentOwner(pageId);

        List<Activity> activities = activityRepository.findByPageIdAndActiveTrueOrderBySortOrderAscIdAsc(pageId);

        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        for (ActivityReorderGroupDto groupDto : groups) {
            Long groupId = groupDto.groupId();

            ActivityGroup group = null;

            if (groupId != null) {
                group = activityGroupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + groupId));

                if (!group.getPage().getId().equals(pageId)) {
                    throw new IllegalArgumentException("Группа не принадлежит этой странице");
                }
            }

            List<Long> activityIds = groupDto.activityIds();

            for (int i = 0; i < activityIds.size(); i++) {
                Long activityId = activityIds.get(i);

                Activity activity = activityMap.get(activityId);

                if (activity == null) {
                    throw new IllegalArgumentException("Дело не найдено на этой странице: " + activityId);
                }

                activity.setGroup(group);
                activity.setSortOrder(i);
            }
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