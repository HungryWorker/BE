package com._2.hungryworker.domain.review.dto;

/**
 * 리뷰 추천/비추천 처리 결과. myReaction은 이번 처리 후 내가 남긴 반응이며,
 * 같은 반응을 다시 눌러 취소한 경우 null이 된다.
 */
public record ReviewReactionResponse(
        Long reviewId,
        long likeCount,
        long dislikeCount,
        String myReaction
) {
}