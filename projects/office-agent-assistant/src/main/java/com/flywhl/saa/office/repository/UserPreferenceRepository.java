package com.flywhl.saa.office.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.flywhl.saa.office.model.entity.UserPreference;
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUserId(Long userId);
    Optional<UserPreference> findByUserIdAndPrefKey(Long userId, String prefKey);
}

