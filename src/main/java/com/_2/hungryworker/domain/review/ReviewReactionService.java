package com._2.hungryworker.domain.review;

import com._2.hungryworker.domain.review.dto.ReviewReactionRequest;
import com._2.hungryworker.domain.review.dto.ReviewReactionResponse;
import com._2.hungryworker.domain.user.User;
import com._2.hungryworker.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewReactionService {

    private final ReviewReactionRepository reviewReactionRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewReactionService(
            ReviewReactionRepository reviewReactionRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository
    ) {
        this.reviewReactionRepository = reviewReactionRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public ReviewReactionResponse react(
            Long userId,
            Long reviewId,
            ReviewReactionRequest request
    ) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        ReviewReaction existing = reviewReactionRepository
                .findByReviewIdAndUserId(reviewId, userId)
                .orElse(null);

        if (existing == null) {
            ReviewReaction reaction = ReviewReaction.builder()
                    .review(review)
                    .user(user)
                    .type(request.type())
                    .build();

            reviewReactionRepository.save(reaction);

        } else if (existing.getType() == request.type()) {
            // 같은 반응을 다시 누르면 취소
            reviewReactionRepository.deleteByReviewIdAndUserId(reviewId, userId);

        } else {
            // LIKE ↔ DISLIKE 변경
            existing.changeType(request.type());
        }

        long likeCount = reviewReactionRepository
                .countByReviewIdAndType(reviewId, ReactionType.LIKE);

        long dislikeCount = reviewReactionRepository
                .countByReviewIdAndType(reviewId, ReactionType.DISLIKE);

        String myReaction = reviewReactionRepository
                .findByReviewIdAndUserId(reviewId, userId)
                .map(reaction -> reaction.getType().name())
                .orElse(null);

        return new ReviewReactionResponse(
                reviewId,
                likeCount,
                dislikeCount,
                myReaction
        );
    }
}