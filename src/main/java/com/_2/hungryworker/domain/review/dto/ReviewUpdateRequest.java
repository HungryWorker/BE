package com._2.hungryworker.domain.review.dto;

import jakarta.validation.constraints.Size;

/**
 * 리뷰 수정 요청. 작성 시와 동일하게 4개 항목 모두 선택 값이다.
 * (이미지 수정은 별도 API - POST/DELETE /api/reviews/{id}/images - 로 처리한다)
 */
public record ReviewUpdateRequest(
        @Size(max = 1000, message = "리뷰 내용은 1000자를 넘을 수 없습니다.")
        String content,
        Double tasteScore,
        Double portionScore,
        Double valueScore,
        Double hygieneScore
) {
}