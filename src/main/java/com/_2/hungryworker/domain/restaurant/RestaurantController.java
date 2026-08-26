package com._2.hungryworker.domain.restaurant;

import com._2.hungryworker.domain.restaurant.client.GooglePlacesClient;
import com._2.hungryworker.domain.restaurant.client.GooglePlacesClient.GoogleTextSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final GooglePlacesClient googlePlacesClient;
    private final RestaurantRepository restaurantRepository;

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