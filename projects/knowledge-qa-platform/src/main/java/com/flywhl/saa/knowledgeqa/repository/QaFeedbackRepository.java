package com.flywhl.saa.knowledgeqa.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.knowledgeqa.model.entity.QaFeedback;

/**
 * qa_feedback JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface QaFeedbackRepository extends JpaRepository<QaFeedback, Long> {

    Optional<QaFeedback> findByMessageIdAndUserId(Long messageId, Long userId);

    long countByRating(Short rating);

    long countByCreatedAtAfter(OffsetDateTime after);

    long countByRatingAndCreatedAtAfter(Short rating, OffsetDateTime after);
}
