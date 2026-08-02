package dev.egor.galochkiapp.activity;

import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
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

        if (groups == null) {
            throw new IllegalArgumentException("Структура дел не может быть null");
        }

        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        List<ActivityGroup> pageGroups =
                activityGroupRepository.findByPageIdOrderBySortOrderAscIdAsc(pageId);
        Map<Long, ActivityGroup> groupMap = pageGroups.stream()
                .collect(Collectors.toMap(ActivityGroup::getId, group -> group));

        Set<Long> suppliedGroupIds = new HashSet<>();
        Set<Long> suppliedActivityIds = new HashSet<>();
        boolean ungroupedSupplied = false;

        for (ActivityReorderGroupDto groupDto : groups) {
            if (groupDto == null) {
                throw new IllegalArgumentException("Элемент структуры дел не может быть null");
            }

            Long groupId = groupDto.groupId();

            if (groupId != null) {
                if (!suppliedGroupIds.add(groupId)) {
                    throw new IllegalArgumentException("Группа передана повторно: " + groupId);
                }

                if (!groupMap.containsKey(groupId)) {
                    throw new IllegalArgumentException("Группа не найдена на этой странице: " + groupId);
                }
            } else if (ungroupedSupplied) {
                throw new IllegalArgumentException("Блок без группы передан повторно");
            } else {
                ungroupedSupplied = true;
            }

            List<Long> activityIds = groupDto.activityIds();

            if (activityIds == null) {
                throw new IllegalArgumentException("Список дел не может быть null");
            }

            for (Long activityId : activityIds) {
                if (activityId == null) {
                    throw new IllegalArgumentException("Идентификатор дела не может быть null");
                }

                if (!suppliedActivityIds.add(activityId)) {
                    throw new IllegalArgumentException("Дело передано повторно: " + activityId);
                }

                if (!activityMap.containsKey(activityId)) {
                    throw new IllegalArgumentException("Дело не найдено на этой странице: " + activityId);
                }
            }
        }

        if (!suppliedGroupIds.equals(groupMap.keySet())) {
            throw new IllegalArgumentException("Передана неполная структура групп");
        }

        if (!suppliedActivityIds.equals(activityMap.keySet())) {
            throw new IllegalArgumentException("Передана неполная структура дел");
        }

        for (ActivityReorderGroupDto groupDto : groups) {
            ActivityGroup group = groupDto.groupId() == null
                    ? null
                    : groupMap.get(groupDto.groupId());

            for (int i = 0; i < groupDto.activityIds().size(); i++) {
                Activity activity = activityMap.get(groupDto.activityIds().get(i));
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
