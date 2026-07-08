package com._2.hungryworker.domain.review;

/**
 * 리뷰 추천/비추천 타입.
 * 별도 타입 테이블 대신 ENUM으로 관리해서 조인 없이 바로 필터링/카운트 가능.
 */
public enum ReactionType {
    LIKE,
    DISLIKE
}
