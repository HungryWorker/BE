package com._2.hungryworker.domain.review;

import com._2.hungryworker.common.BaseTimeEntity;
import com._2.hungryworker.domain.restaurant.Restaurant;
import com._2.hungryworker.domain.user.User;
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
 * 식당 리뷰. 한 유저가 같은 식당에 여러 개의 리뷰를 남길 수 있도록
 * (user_id, restaurant_id)에 유니크 제약을 걸지 않는다 (의도적).
 */
@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 1000)
    private String content;

    // 1~5 범위는 서비스 레이어에서 검증
    @Column(nullable = false)
    private Integer rating;

    @Builder
    public Review(User user, Restaurant restaurant, String content, Integer rating) {
        this.user = user;
        this.restaurant = restaurant;
        this.content = content;
        this.rating = rating;
    }

    public void update(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }
}
