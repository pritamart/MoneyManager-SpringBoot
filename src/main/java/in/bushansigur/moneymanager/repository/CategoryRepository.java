package in.bushansigur.moneymanager.repository;

import in.bushansigur.moneymanager.entity.CategoryEntity;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // ✅ If you want to fetch all categories by Profile entity
    List<CategoryEntity> findAllByProfile(ProfileEntity profile);

    // ✅ Find by category ID and Profile entity
    Optional<CategoryEntity> findByIdAndProfile(Long id, ProfileEntity profile);

    // ✅ Find by type and Profile ID (not Profile object)
    List<CategoryEntity> findByTypeAndProfile_Id(String type, Long profileId);

    // ✅ Check existence by name and Profile ID (use name, not id)
    boolean existsByNameAndProfile_Id(String name, Long profileId);
}
