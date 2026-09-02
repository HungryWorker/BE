package com._2.hungryworker.domain.review;

import com._2.hungryworker.domain.review.dto.ReviewCreateRequest;
import com._2.hungryworker.domain.review.dto.ReviewResponse;
import com._2.hungryworker.domain.review.dto.ReviewSummaryResponse;
import com._2.hungryworker.domain.review.dto.ReviewUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 식당 팝업의 댓글 목록. 비로그인 사용자도 볼 수 있다 (SecurityConfig permitAll).
     */
    @GetMapping("/api/restaurants/{restaurantId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(reviewService.getReviews(restaurantId, userId));
    }

    /**
     * 식당 팝업 상단에 표시할 종합 별점(5점 만점) + 리뷰 개수.
     */
    @GetMapping("/api/restaurants/{restaurantId}/reviews/summary")
    public ResponseEntity<ReviewSummaryResponse> getSummary(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getSummary(restaurantId));
    }

    /**
     * 리뷰 작성. 댓글(content)과 사진(images)은 선택 값이며,
     * 맛/양/값/위생 4개 별점 중 하나만 남겨도 등록할 수 있다 (별점만 등록 가능).
     * multipart/form-data로 전송한다: content, tasteScore, portionScore, valueScore,
     * hygieneScore는 일반 텍스트 파트, images는 파일 파트(여러 개 가능, 선택).
     */
    @PostMapping(value = "/api/restaurants/{restaurantId}/reviews",
            consumes = {"multipart/form-data"})
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long restaurantId,
            @Valid @ModelAttribute ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        ReviewResponse response = reviewService.createReview(userId, restaurantId, request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 리뷰(댓글/별점) 수정. 본인이 작성한 리뷰만 가능하다.
     * 첨부 이미지는 이 API로 바꿀 수 없고 최초 등록 시 사진 그대로 유지된다.
     */
    @PatchMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(reviewService.updateReview(userId, reviewId, request));
    }

    /**
     * 리뷰 삭제. 본인이 작성한 리뷰만 가능하며, 첨부된 사진 파일도 함께 삭제한다.
     */
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.noContent().build();
    }
}