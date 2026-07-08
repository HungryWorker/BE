package com._2.hungryworker.domain.restaurant;

import com._2.hungryworker.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 한 번이라도 검색/조회된 식당. Google Places API 응답을 캐싱하는 개념의 테이블이다.
 */
@Entity
@Table(
    name = "restaurant",
    uniqueConstraints = @UniqueConstraint(name = "uk_restaurant_place_id", columnNames = "google_place_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    @Column(name = "google_place_id", nullable = false, length = 200)
    private String googlePlaceId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String website;

    // 지도 마커 표시를 위해 필수
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantCategory> categories = new ArrayList<>();

    @Builder
    public Restaurant(String googlePlaceId, String name, String address, String phone,
                       String website, Double latitude, Double longitude) {
        this.googlePlaceId = googlePlaceId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void addPhoto(String photoReference) {
        RestaurantPhoto photo = RestaurantPhoto.builder()
            .restaurant(this)
            .photoReference(photoReference)
            .build();
        this.photos.add(photo);
    }

    public void addCategory(Category category) {
        RestaurantCategory restaurantCategory = RestaurantCategory.builder()
            .restaurant(this)
            .category(category)
            .build();
        this.categories.add(restaurantCategory);
    }

    public void updateBasicInfo(String name, String address, String phone, String website) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
    }
}
