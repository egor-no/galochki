package dev.egor.galochkiapp.galochka;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GalochkaRepository extends JpaRepository<Galochka, Long> {

    Optional<Galochka> findByActivityIdAndDate(Long activityId, LocalDate date);

    List<Galochka> findByActivityPageIdAndDateBetween(Long pageId, LocalDate start, LocalDate end);
}