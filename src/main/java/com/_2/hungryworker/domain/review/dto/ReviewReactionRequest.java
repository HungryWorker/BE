package com._2.hungryworker.domain.review.dto;

import com._2.hungryworker.domain.review.ReactionType;
import jakarta.validation.constraints.NotNull;

/**
 * 리뷰 추천/비추천 요청. RestaurantPopup(FE)이 { type: "LIKE" | "DISLIKE" } 형태로 보낸다.
 */
public record ReviewReactionRequest(
        @NotNull(message = "반응 타입(type)은 필수입니다.")
        ReactionType type
) {
}