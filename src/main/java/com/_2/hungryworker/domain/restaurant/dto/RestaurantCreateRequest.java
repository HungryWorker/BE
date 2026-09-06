package com._2.hungryworker.domain.restaurant.dto;

import java.util.List;

/**
 * RestaurantCreatePopup(FE)에서 전송하는 "식당 등록" 요청.
 * multipart/form-data로 전송되며, 사진 파일(photos)만 별도 @RequestPart로 받고
 * 나머지 텍스트 값들은 이 DTO로 바인딩된다.
 *
 * - restaurantId: 이미 검색(/api/restaurants/search)을 통해 캐시된 식당에 추가 정보를
 *   등록하는 경우 전달한다. null이면 name/latitude/longitude로 새 식당을 만든다.
 * - categories: 카테고리 그리드에서 체크된 항목들의 라벨 목록 (예: ["밥", "한식"]).
 * - tags: "기타" 섹션에서 선택/추가된 태그 목록 (예: ["할랄", "비건"]).
 * - menusJson: [{"name":"김치찌개","price":9000}, ...] 형태의 JSON 문자열.
 *   메뉴는 이름+가격 쌍의 리스트라 multipart 폼 필드로 바로 바인딩하기 까다로워
 *   프론트에서 JSON.stringify 해서 보내고 서버에서 파싱한다.
 */
public record RestaurantCreateRequest(
        Long restaurantId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String breakTime,
        List<String> categories,
        List<String> tags,
        String menusJson
) {

    public record MenuItem(String name, Integer price) {
    }
}