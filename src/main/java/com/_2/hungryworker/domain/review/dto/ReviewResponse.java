package com._2.hungryworker.domain.review.dto;

import com._2.hungryworker.domain.review.ReactionType;
import com._2.hungryworker.domain.review.Review;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long id,
        Long restaurantId,
        Long userId,
        String nickname,
        String content,
        double tasteScore,
        double portionScore,
        double valueScore,
        double hygieneScore,
        double rating,
        List<String> imageUrls,
        boolean mine,
        long likeCount,
        long dislikeCount,
        String myReaction,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse of(
            Review review,
            boolean mine,
            long likeCount,
            long dislikeCount,
            ReactionType myReaction
    ) {
        return new ReviewResponse(
                review.getId(),
                review.getRestaurant().getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getContent(),
                review.getTasteScore(),
                review.getPortionScore(),
                review.getValueScore(),
                review.getHygieneScore(),
                review.getRating(),
                review.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .toList(),
                mine,
                likeCount,
                dislikeCount,
                myReaction != null ? myReaction.name() : null,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}