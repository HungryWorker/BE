package com._2.hungryworker.domain.restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 "식당 등록" 팝업에서 직접 업로드한 식당 사진.
 * Google Places 캐시 사진(RestaurantPhoto.photoReference)과는 별도로 관리한다.
 * imageUrl에는 FileStorageService가 반환하는 정적 리소스 경로(예: /uploads/restaurants/xxx.jpg)가 저장된다.
 */
@Entity
@Table(name = "restaurant_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Builder
    public RestaurantImage(Restaurant restaurant, String imageUrl) {
        this.restaurant = restaurant;
        this.imageUrl = imageUrl;
    }
}