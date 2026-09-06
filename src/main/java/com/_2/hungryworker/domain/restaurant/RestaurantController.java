package com._2.hungryworker.domain.restaurant;

import com._2.hungryworker.domain.restaurant.client.GooglePlacesClient;
import com._2.hungryworker.domain.restaurant.client.GooglePlacesClient.GoogleTextSearchResponse;
import com._2.hungryworker.domain.restaurant.dto.RestaurantCreateRequest;
import com._2.hungryworker.domain.restaurant.dto.RestaurantDetailResponse;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private static final double EARTH_RADIUS_M = 6371000;

    private final GooglePlacesClient googlePlacesClient;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;

    /**
     * 지도 중심 좌표 주변의, 이미 등록/캐시된 식당 목록을 반환한다.
     * (Google API를 매번 호출하지 않고 DB에 저장된 식당만 대상으로 거리순 필터링한다.)
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<RestaurantController.RestaurantResponse>> getNearbyRestaurants(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "1500") Double radius
    ) {
        List<RestaurantResponse> nearby = restaurantRepository.findAll().stream()
                .filter(r -> r.getLatitude() != null && r.getLongitude() != null)
                .filter(r -> distanceMeters(lat, lng, r.getLatitude(), r.getLongitude()) <= radius)
                .sorted(Comparator.comparingDouble(
                        r -> distanceMeters(lat, lng, r.getLatitude(), r.getLongitude())))
                .map(RestaurantResponse::from)
                .toList();

        return ResponseEntity.ok(nearby);
    }

    /**
     * 식당 등록 팝업(RestaurantCreatePopup)의 "등록" 버튼 처리.
     * multipart/form-data로 전송되며, name/breakTime/categories/tags/menusJson 등은
     * 일반 폼 필드로, photos는 파일 파트(최소 2장)로 전달된다.
     * 로그인한 사용자만 등록할 수 있다 (SecurityConfig 기본 정책: 미명시 경로는 인증 필요).
     */
    @PostMapping(value = "", consumes = {"multipart/form-data"})
    public ResponseEntity<RestaurantDetailResponse> createRestaurant(
            @Valid @ModelAttribute RestaurantCreateRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        RestaurantDetailResponse response = restaurantService.registerRestaurant(request, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sinDLat = Math.sin(dLat / 2);
        double sinDLng = Math.sin(dLng / 2);
        double h = sinDLat * sinDLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinDLng * sinDLng;
        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(h));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
            @RequestParam String keyword,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "1500") Integer radius
    ) {
        GoogleTextSearchResponse response =
                googlePlacesClient.searchText(keyword, lat, lng, radius);

        if (response == null || response.places() == null) {
            return ResponseEntity.ok(List.of());
        }

        List<RestaurantResponse> restaurants = response.places().stream()
                .map(place -> {
                    Restaurant restaurant = restaurantRepository
                            .findByGooglePlaceId(place.id())
                            .orElseGet(() -> restaurantRepository.save(
                                    Restaurant.builder()
                                            .googlePlaceId(place.id())
                                            .name(place.displayName() != null
                                                    ? place.displayName().text()
                                                    : "이름 없음")
                                            .address(place.formattedAddress())
                                            .latitude(place.location().latitude())
                                            .longitude(place.location().longitude())
                                            .build()
                            ));

                    return RestaurantResponse.from(restaurant);
                })
                .toList();

        return ResponseEntity.ok(restaurants);
    }

    public record RestaurantResponse(
            Long id,
            String googlePlaceId,
            String name,
            String address,
            Double latitude,
            Double longitude
    ) {
        public static RestaurantResponse from(Restaurant restaurant) {
            return new RestaurantResponse(
                    restaurant.getId(),
                    restaurant.getGooglePlaceId(),
                    restaurant.getName(),
                    restaurant.getAddress(),
                    restaurant.getLatitude(),
                    restaurant.getLongitude()
            );
        }
    }
}