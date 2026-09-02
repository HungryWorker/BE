package com._2.hungryworker.domain.review.dto;

/**
 * 식당 팝업 상단에 표시되는 종합 별점 정보.
 * totalScore는 모든 리뷰의 rating(4개 항목 평균) 값을 다시 평균 낸 5점 만점 점수다.
 */
public record ReviewSummaryResponse(
        Long restaurantId,
        long reviewCount,
        double totalScore,
        double avgTasteScore,
        double avgPortionScore,
        double avgValueScore,
        double avgHygieneScore
) {
    public static ReviewSummaryResponse empty(Long restaurantId) {
        return new ReviewSummaryResponse(restaurantId, 0, 0, 0, 0, 0, 0);
    }
}