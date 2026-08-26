package com._2.hungryworker.domain.restaurant.client.dto;

/**
 * Text Search (New) 요청 바디.
 * POST https://places.googleapis.com/v1/places:searchText
 *
 * locationBias는 좌표가 주어졌을 때만 채워서, 위치 정보 없이 순수 키워드로도
 * 검색할 수 있게 한다.
 */
public record GoogleTextSearchRequest(
        String textQuery,
        LocationBias locationBias,
        int maxResultCount
) {

    private static final int DEFAULT_MAX_RESULT_COUNT = 20;
    private static final double DEFAULT_RADIUS_METERS = 1500.0;

    public static GoogleTextSearchRequest of(String keyword, Double lat, Double lng, Integer radiusMeters) {
        LocationBias locationBias = (lat != null && lng != null)
                ? new LocationBias(new Circle(
                new LatLng(lat, lng),
                radiusMeters != null ? radiusMeters.doubleValue() : DEFAULT_RADIUS_METERS))
                : null;
        return new GoogleTextSearchRequest(keyword, locationBias, DEFAULT_MAX_RESULT_COUNT);
    }

    public record LocationBias(Circle circle) {
    }

    public record Circle(LatLng center, double radius) {
    }

    public record LatLng(double latitude, double longitude) {
    }
}