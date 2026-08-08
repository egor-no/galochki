package dev.egor.galochkiapp.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByPageIdAndGroupIdAndActiveTrueOrderBySortOrderAscIdAsc(Long pageId, Long groupId);

    List<Activity> findByPageIdAndGroupIdOrderBySortOrderAscIdAsc(Long pageId, Long groupId);

    List<Activity> findByPageIdAndGroupIsNullAndActiveTrueOrderBySortOrderAscIdAsc(Long pageId);

    List<Activity> findByPageIdAndGroupIsNullOrderBySortOrderAscIdAsc(Long pageId);

    List<Activity> findByPageIdAndActiveTrueOrderBySortOrderAscIdAsc(Long pageId);

    List<Activity> findByPageIdAndActiveTrueOrderByGroupSortOrderAscSortOrderAscIdAsc(Long pageId);

    int countByPageIdAndActiveTrue(Long pageId);

    int countByPageIdAndGroupIdAndActiveTrue(Long pageId, Long groupId);

    void deleteByPageId(Long pageId);
}
