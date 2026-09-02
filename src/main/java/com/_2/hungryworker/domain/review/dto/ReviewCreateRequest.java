package com._2.hungryworker.domain.review.dto;

import jakarta.validation.constraints.Size;

/**
 * 리뷰 작성 요청. multipart/form-data의 텍스트 파트로 전달된다.
 * content, tasteScore, portionScore, valueScore, hygieneScore 모두 선택 값이며,
 * 넷 중 하나(댓글/사진/별점) 이상만 채워져 있으면 등록 가능하다 (검증은 서비스 레이어에서 수행).
 */
public record ReviewCreateRequest(
        @Size(max = 1000, message = "리뷰 내용은 1000자를 넘을 수 없습니다.")
        String content,
        Double tasteScore,
        Double portionScore,
        Double valueScore,
        Double hygieneScore
) {
}