package com._2.hungryworker.domain.restaurant.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Text Search (New) 응답의 개별 place. 검색 단계에서는 과금을 낮추기 위해
 * id / displayName / formattedAddress / location만 요청한다 (Essentials 등급).
 * 나머지 상세 정보(전화번호, 웹사이트, 사진, 카테고리)는 DB에 없는 장소에 한해
 * GooglePlaceDetail로 별도 조회한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GooglePlaceCandidate(
        String id,
        DisplayName displayName,
        String formattedAddress,
        GoogleLatLng location
) {

    public String name() {
        return displayName != null ? displayName.text() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DisplayName(String text, String languageCode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoogleLatLng(Double latitude, Double longitude) {
    }
}