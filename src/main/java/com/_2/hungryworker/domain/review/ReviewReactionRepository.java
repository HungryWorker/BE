package com._2.hungryworker.domain.review;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {

    Optional<ReviewReaction> findByReviewIdAndUserId(Long reviewId, Long userId);

    long countByReviewIdAndType(Long reviewId, ReactionType type);

    void deleteByReviewIdAndUserId(Long reviewId, Long userId);
}