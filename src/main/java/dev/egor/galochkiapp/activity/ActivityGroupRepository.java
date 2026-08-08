package dev.egor.galochkiapp.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityGroupRepository extends JpaRepository<ActivityGroup, Long> {

    List<ActivityGroup> findByPageIdOrderBySortOrderAscIdAsc(Long pageId);

    int countByPageId(Long pageId);

    void deleteByPageId(Long pageId);
}