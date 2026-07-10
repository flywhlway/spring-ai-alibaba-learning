package com.flywhl.saa.smartcs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.entity.ModelProfile;

/**
 * model_profile JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface ModelProfileRepository extends JpaRepository<ModelProfile, Long> {

    Optional<ModelProfile> findByProfileKey(String profileKey);

    List<ModelProfile> findByScene(String scene);

    List<ModelProfile> findBySceneAndEnabledTrueOrderByPriorityDesc(String scene);

    boolean existsByProfileKey(String profileKey);
}
