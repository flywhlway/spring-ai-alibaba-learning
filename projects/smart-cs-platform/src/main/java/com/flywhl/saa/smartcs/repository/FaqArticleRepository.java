package com.flywhl.saa.smartcs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.FaqArticleStatus;
import com.flywhl.saa.smartcs.model.entity.FaqArticle;

/**
 * faq_article JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface FaqArticleRepository extends JpaRepository<FaqArticle, Long> {

    Page<FaqArticle> findByCategoryAndStatus(String category, FaqArticleStatus status, Pageable pageable);

    Page<FaqArticle> findByStatus(FaqArticleStatus status, Pageable pageable);

    Page<FaqArticle> findByCategory(String category, Pageable pageable);

    boolean existsByTitle(String title);
}
