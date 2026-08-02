package dev.egor.galochkiapp.week;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PageWeekOverheadRepository extends JpaRepository<PageWeekOverhead, Long> {

    Optional<PageWeekOverhead> findByPageIdAndWeekStartDate(Long pageId, LocalDate weekStartDate);

    List<PageWeekOverhead> findByPageIdOrderByWeekStartDateAsc(Long pageId);

    List<PageWeekOverhead> findByPageIdAndWeekStartDateBetween(Long pageId, LocalDate start, LocalDate end);

    void deleteByPageIdAndWeekStartDate(Long pageId, LocalDate weekStartDate);

    void deleteByPageId(Long pageId);
}