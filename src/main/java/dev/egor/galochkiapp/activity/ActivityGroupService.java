package dev.egor.galochkiapp.activity;

import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityGroupService {

    private final ActivityGroupRepository groupRepository;
    private final ActivityRepository activityRepository;
    private final GalochkiPageService pageService;

    public ActivityGroupService(ActivityGroupRepository groupRepository,
                                ActivityRepository activityRepository,
                                GalochkiPageService pageService) {
        this.groupRepository = groupRepository;
        this.activityRepository = activityRepository;
        this.pageService = pageService;
    }

    @Transactional
    public ActivityGroup create(Long pageId, String title) {
        GalochkiPage page = pageService.getByIdForCurrentOwner(pageId);

        ActivityGroup group = new ActivityGroup();
        group.setPage(page);
        group.setTitle(title);
        group.setSortOrder(groupRepository.countByPageId(pageId));

        return groupRepository.save(group);
    }

    @Transactional
    public void renameForCurrentOwner(Long groupId, String title) {
        ActivityGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + groupId));

        pageService.getByIdForCurrentOwner(group.getPage().getId());

        group.setTitle(title);
    }

    @Transactional
    public void reorderGroupsForCurrentOwner(Long pageId, List<Long> groupIds) {
        pageService.getByIdForCurrentOwner(pageId);

        List<ActivityGroup> groups = groupRepository.findByPageIdOrderBySortOrderAscIdAsc(pageId);

        Map<Long, ActivityGroup> groupMap = groups.stream()
                .collect(Collectors.toMap(ActivityGroup::getId, group -> group));

        for (int i = 0; i < groupIds.size(); i++) {
            ActivityGroup group = groupMap.get(groupIds.get(i));

            if (group == null) {
                throw new IllegalArgumentException("Группа не найдена на этой странице: " + groupIds.get(i));
            }

            group.setSortOrder(i);
        }
    }

    @Transactional
    public void deleteForCurrentOwner(Long groupId) {
        ActivityGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + groupId));

        Long pageId = group.getPage().getId();

        pageService.getByIdForCurrentOwner(pageId);

        List<Activity> groupActivities =
                activityRepository.findByPageIdAndGroupIdAndActiveTrueOrderBySortOrderAscIdAsc(pageId, groupId);

        int sortOrder = activityRepository
                .findByPageIdAndGroupIsNullAndActiveTrueOrderBySortOrderAscIdAsc(pageId)
                .size();

        for (Activity activity : groupActivities) {
            activity.setGroup(null);
            activity.setSortOrder(sortOrder);
            sortOrder++;
        }

        groupRepository.delete(group);
    }
}