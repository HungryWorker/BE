package com._2.hungryworker.domain.restaurant.dto;

import com._2.hungryworker.domain.restaurant.Restaurant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 식당 등록/상세 조회에 사용하는 응답. RestaurantPopup(FE)이 기대하는
 * name/address/breakTime/tags/photos/cheapestMenu 등의 필드를 채워준다.
 */
public record RestaurantDetailResponse(
        Long id,
        String googlePlaceId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String breakTime,
        List<String> categories,
        List<String> tags,
        List<MenuResponse> menus,
        List<String> photos,
        String cheapestMenu,
        Integer cheapestPrice,
        LocalDateTime registeredAt
) {

    public record MenuResponse(Long id, String name, int price) {
    }

    public static RestaurantDetailResponse from(Restaurant restaurant) {
        List<MenuResponse> menus = restaurant.getMenus().stream()
                .map(menu -> new MenuResponse(menu.getId(), menu.getName(), menu.getPrice()))
                .toList();

        MenuResponse cheapest = menus.stream()
                .min(Comparator.comparingInt(MenuResponse::price))
                .orElse(null);

        List<String> categories = restaurant.getCategories().stream()
                .map(rc -> rc.getCategory().getName())
                .toList();

        List<String> tags = restaurant.getTags().stream()
                .map(rt -> rt.getTag().getName())
                .toList();

        List<String> photos = restaurant.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList();

        return new RestaurantDetailResponse(
                restaurant.getId(),
                restaurant.getGooglePlaceId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                restaurant.getBreakTime(),
                categories,
                tags,
                menus,
                photos,
                cheapest != null ? cheapest.name() : null,
                cheapest != null ? cheapest.price() : null,
                restaurant.getCreatedAt()
        );
    }
}