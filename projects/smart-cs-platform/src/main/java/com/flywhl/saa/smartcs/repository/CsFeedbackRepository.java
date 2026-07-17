package com.flywhl.saa.smartcs.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.entity.CsFeedback;

/**
 * cs_feedback JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface CsFeedbackRepository extends JpaRepository<CsFeedback, Long> {

    boolean existsByMessageIdAndUserId(Long messageId, Long userId);

    long countByCreatedAtAfter(OffsetDateTime after);

    long countByRatingAndCreatedAtAfter(Short rating, OffsetDateTime after);
}
