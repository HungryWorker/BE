package com._2.hungryworker.domain.restaurant;

import com._2.hungryworker.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

    // 사용자가 "식당 등록" 팝업에서 자유롭게 입력하는 브레이크타임 문구 (예: "15:00-17:00")
    @Column(name = "break_time", length = 100)
    private String breakTime;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantCategory> categories = new ArrayList<>();

    // 사용자가 업로드한 식당 사진 (Google 캐시 사진과 별도로 관리)
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<RestaurantImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("price asc")
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantTag> tags = new ArrayList<>();

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
        boolean alreadyLinked = this.categories.stream()
                .anyMatch(rc -> rc.getCategory().getId().equals(category.getId()));
        if (alreadyLinked) {
            return;
        }
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

    public void updateBreakTime(String breakTime) {
        this.breakTime = breakTime;
    }

    public void addImage(String imageUrl) {
        RestaurantImage image = RestaurantImage.builder()
                .restaurant(this)
                .imageUrl(imageUrl)
                .build();
        this.images.add(image);
    }

    public void addMenu(String name, int price) {
        Menu menu = Menu.builder()
                .restaurant(this)
                .name(name)
                .price(price)
                .build();
        this.menus.add(menu);
    }

    public void addTag(Tag tag) {
        boolean alreadyLinked = this.tags.stream()
                .anyMatch(rt -> rt.getTag().getId().equals(tag.getId()));
        if (alreadyLinked) {
            return;
        }
        RestaurantTag restaurantTag = RestaurantTag.builder()
                .restaurant(this)
                .tag(tag)
                .build();
        this.tags.add(restaurantTag);
    }
}