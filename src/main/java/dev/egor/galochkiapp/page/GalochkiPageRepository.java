package dev.egor.galochkiapp.page;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GalochkiPageRepository extends JpaRepository<GalochkiPage, Long> {

    List<GalochkiPage> findByOwnerIdOrderById(Long ownerId);

    Optional<GalochkiPage> findFirstByOwnerIdOrderById(Long ownerId);

    Optional<GalochkiPage> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerId(Long ownerId);
}