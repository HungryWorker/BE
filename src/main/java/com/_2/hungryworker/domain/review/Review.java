package com._2.hungryworker.domain.review;

import com._2.hungryworker.common.BaseTimeEntity;
import com._2.hungryworker.domain.restaurant.Restaurant;
import com._2.hungryworker.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 식당 리뷰. 한 유저가 같은 식당에 여러 개의 리뷰를 남길 수 있도록
 * (user_id, restaurant_id)에 유니크 제약을 걸지 않는다 (의도적).
 *
 * 댓글(content)과 사진(images)은 선택 사항이며, 맛/양/값/위생 4개 세부 별점 중
 * 하나라도 남기면 그 자체로 리뷰가 될 수 있다 (별점만 등록 가능).
 * rating(총점)은 4개 세부 별점의 평균으로 서버에서 자동 계산되며, 값을 남기지 않은
 * 항목은 0점으로 취급되어 평균에 그대로 반영된다.
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

    // 댓글 없이 별점만 등록할 수 있으므로 nullable
    @Column(length = 1000)
    private String content;

    // 0~5, 0.5 단위. 값을 남기지 않으면 0점.
    @Column(name = "taste_score", nullable = false)
    private double tasteScore;

    @Column(name = "portion_score", nullable = false)
    private double portionScore;

    @Column(name = "value_score", nullable = false)
    private double valueScore;

    @Column(name = "hygiene_score", nullable = false)
    private double hygieneScore;

    // taste/portion/value/hygiene 4개 항목의 평균 (자동 계산, 5점 만점)
    @Column(nullable = false)
    private double rating;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<ReviewImage> images = new ArrayList<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Review(User user, Restaurant restaurant, String content,
                  double tasteScore, double portionScore, double valueScore, double hygieneScore) {
        this.user = user;
        this.restaurant = restaurant;
        this.content = content;
        this.tasteScore = tasteScore;
        this.portionScore = portionScore;
        this.valueScore = valueScore;
        this.hygieneScore = hygieneScore;
        recalculateRating();
    }

    public void update(String content, double tasteScore, double portionScore,
                       double valueScore, double hygieneScore) {
        this.content = content;
        this.tasteScore = tasteScore;
        this.portionScore = portionScore;
        this.valueScore = valueScore;
        this.hygieneScore = hygieneScore;
        recalculateRating();
    }

    public void addImage(String imageUrl) {
        ReviewImage image = ReviewImage.builder()
                .review(this)
                .imageUrl(imageUrl)
                .build();
        this.images.add(image);
    }

    private void recalculateRating() {
        double sum = tasteScore + portionScore + valueScore + hygieneScore;
        // 소수 첫째 자리까지 반올림
        this.rating = Math.round((sum / 4.0) * 10) / 10.0;
    }
}