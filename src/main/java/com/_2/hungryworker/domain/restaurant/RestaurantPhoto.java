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
 * 식당 하나에 사진이 여러 장 있을 수 있어 별도 테이블로 분리.
 */
@Entity
@Table(name = "restaurant_photo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Google Places photo_reference 값
    @Column(name = "photo_reference", nullable = false, length = 500)
    private String photoReference;

    @Builder
    public RestaurantPhoto(Restaurant restaurant, String photoReference) {
        this.restaurant = restaurant;
        this.photoReference = photoReference;
    }
}
