package com._2.hungryworker.domain.restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Google Places API의 types 값(예: restaurant, cafe, bakery)을 정규화한 테이블.
 * 식당당 여러 개일 수 있어 RestaurantCategory와 M:N 관계를 맺는다.
 */
@Entity
@Table(
    name = "category",
    uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = "name")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Builder
    public Category(String name) {
        this.name = name;
    }
}
