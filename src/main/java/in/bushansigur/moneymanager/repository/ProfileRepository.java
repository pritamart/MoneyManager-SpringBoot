package in.bushansigur.moneymanager.repository;

import in.bushansigur.moneymanager.dto.ProfileDTO;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity,Long> {
    Optional<ProfileEntity> findByEmail(String email);

    Optional<ProfileEntity> findByActivationToken(String activationToken);
}