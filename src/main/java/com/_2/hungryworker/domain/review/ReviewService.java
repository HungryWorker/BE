package com._2.hungryworker.domain.review;

import com._2.hungryworker.domain.restaurant.Restaurant;
import com._2.hungryworker.domain.restaurant.RestaurantRepository;
import com._2.hungryworker.domain.review.dto.ReviewCreateRequest;
import com._2.hungryworker.domain.review.dto.ReviewResponse;
import com._2.hungryworker.domain.review.dto.ReviewSummaryResponse;
import com._2.hungryworker.domain.review.dto.ReviewUpdateRequest;
import com._2.hungryworker.domain.user.User;
import com._2.hungryworker.domain.user.UserRepository;
import com._2.hungryworker.global.file.FileStorageService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private static final String REVIEW_IMAGE_SUB_DIR = "reviews";
    private static final int MAX_IMAGE_COUNT = 5;

    private final ReviewRepository reviewRepository;
    private final ReviewReactionRepository reviewReactionRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewReactionRepository reviewReactionRepository,
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewReactionRepository = reviewReactionRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<ReviewResponse> getReviews(Long restaurantId, Long viewerId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(review -> {
                    long likeCount = reviewReactionRepository
                            .countByReviewIdAndType(
                                    review.getId(),
                                    ReactionType.LIKE
                            );

                    long dislikeCount = reviewReactionRepository
                            .countByReviewIdAndType(
                                    review.getId(),
                                    ReactionType.DISLIKE
                            );

                    ReactionType myReaction = null;

                    if (viewerId != null) {
                        myReaction = reviewReactionRepository
                                .findByReviewIdAndUserId(review.getId(), viewerId)
                                .map(ReviewReaction::getType)
                                .orElse(null);
                    }

                    return ReviewResponse.of(
                            review,
                            isMine(review, viewerId),
                            likeCount,
                            dislikeCount,
                            myReaction
                    );
                })
                .toList();
    }

    public ReviewSummaryResponse getSummary(Long restaurantId) {
        long reviewCount = reviewRepository.countByRestaurantId(restaurantId);

        if (reviewCount == 0) {
            return ReviewSummaryResponse.empty(restaurantId);
        }

        double totalScore = reviewRepository
                .findAverageRatingByRestaurantId(restaurantId)
                .orElse(0.0);

        double avgTaste = reviewRepository
                .findAverageTasteScoreByRestaurantId(restaurantId)
                .orElse(0.0);

        double avgPortion = reviewRepository
                .findAveragePortionScoreByRestaurantId(restaurantId)
                .orElse(0.0);

        double avgValue = reviewRepository
                .findAverageValueScoreByRestaurantId(restaurantId)
                .orElse(0.0);

        double avgHygiene = reviewRepository
                .findAverageHygieneScoreByRestaurantId(restaurantId)
                .orElse(0.0);

        return new ReviewSummaryResponse(
                restaurantId,
                reviewCount,
                round1(totalScore),
                round1(avgTaste),
                round1(avgPortion),
                round1(avgValue),
                round1(avgHygiene)
        );
    }

    @Transactional
    public ReviewResponse createReview(
            Long userId,
            Long restaurantId,
            ReviewCreateRequest request,
            List<MultipartFile> images
    ) {
        User user = findUserOrThrow(userId);
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);

        double taste = validateScore(request.tasteScore(), "맛");
        double portion = validateScore(request.portionScore(), "양");
        double value = validateScore(request.valueScore(), "값");
        double hygiene = validateScore(request.hygieneScore(), "위생");

        String content = normalizeContent(request.content());
        List<MultipartFile> validImages = filterValidImages(images);

        validateNotEmpty(
                content,
                validImages,
                taste,
                portion,
                value,
                hygiene
        );

        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .content(content)
                .tasteScore(taste)
                .portionScore(portion)
                .valueScore(value)
                .hygieneScore(hygiene)
                .build();

        attachImages(review, validImages);

        Review saved = reviewRepository.save(review);

        return ReviewResponse.of(
                saved,
                true,
                0,
                0,
                null
        );
    }

    @Transactional
    public ReviewResponse updateReview(
            Long userId,
            Long reviewId,
            ReviewUpdateRequest request
    ) {
        Review review = findReviewOrThrow(reviewId);

        validateOwner(review, userId);

        double taste = validateScore(request.tasteScore(), "맛");
        double portion = validateScore(request.portionScore(), "양");
        double value = validateScore(request.valueScore(), "값");
        double hygiene = validateScore(request.hygieneScore(), "위생");

        String content = normalizeContent(request.content());

        boolean hasExistingImages = !review.getImages().isEmpty();

        // 수정 시 기존 첨부 이미지가 남아 있다면
        // 댓글/별점이 모두 비어 있어도 리뷰 자체는 유효하다.
        if (!hasExistingImages) {
            validateNotEmpty(
                    content,
                    List.of(),
                    taste,
                    portion,
                    value,
                    hygiene
            );
        }

        review.update(
                content,
                taste,
                portion,
                value,
                hygiene
        );

        long likeCount = reviewReactionRepository
                .countByReviewIdAndType(
                        reviewId,
                        ReactionType.LIKE
                );

        long dislikeCount = reviewReactionRepository
                .countByReviewIdAndType(
                        reviewId,
                        ReactionType.DISLIKE
                );

        ReactionType myReaction = reviewReactionRepository
                .findByReviewIdAndUserId(reviewId, userId)
                .map(ReviewReaction::getType)
                .orElse(null);

        return ReviewResponse.of(
                review,
                true,
                likeCount,
                dislikeCount,
                myReaction
        );
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = findReviewOrThrow(reviewId);

        validateOwner(review, userId);

        review.getImages().forEach(image ->
                fileStorageService.delete(image.getImageUrl())
        );

        // 리뷰를 삭제하기 전에 해당 리뷰의 리액션을 먼저 삭제한다.
        reviewReactionRepository.deleteByReviewId(reviewId);

        reviewRepository.delete(review);
    }

    private void attachImages(
            Review review,
            List<MultipartFile> images
    ) {
        for (MultipartFile image : images) {
            String storedUrl = fileStorageService.store(
                    image,
                    REVIEW_IMAGE_SUB_DIR
            );

            review.addImage(storedUrl);
        }
    }

    private List<MultipartFile> filterValidImages(
            List<MultipartFile> images
    ) {
        if (images == null) {
            return List.of();
        }

        List<MultipartFile> filtered = images.stream()
                .filter(image ->
                        image != null && !image.isEmpty()
                )
                .toList();

        if (filtered.size() > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException(
                    "사진은 최대 " + MAX_IMAGE_COUNT + "장까지 첨부할 수 있습니다."
            );
        }

        return filtered;
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        return content.trim();
    }

    private void validateNotEmpty(
            String content,
            List<MultipartFile> images,
            double taste,
            double portion,
            double value,
            double hygiene
    ) {
        boolean hasContent = content != null;
        boolean hasImages = !images.isEmpty();

        boolean hasScore =
                taste > 0
                        || portion > 0
                        || value > 0
                        || hygiene > 0;

        if (!hasContent && !hasImages && !hasScore) {
            throw new IllegalArgumentException(
                    "댓글, 사진, 별점 중 하나는 반드시 입력해야 합니다."
            );
        }
    }

    /**
     * 별점은 0~5 사이, 0.5 단위로만 허용한다.
     * null이면 입력하지 않은 것으로 보고 0점으로 취급한다.
     */
    private double validateScore(
            Double score,
            String label
    ) {
        if (score == null) {
            return 0.0;
        }

        if (score < 0 || score > 5) {
            throw new IllegalArgumentException(
                    label + " 별점은 0~5 사이여야 합니다."
            );
        }

        double doubled = score * 2;

        if (Math.abs(doubled - Math.round(doubled)) > 1e-6) {
            throw new IllegalArgumentException(
                    label + " 별점은 0.5 단위로만 입력할 수 있습니다."
            );
        }

        return Math.round(doubled) / 2.0;
    }

    private boolean isMine(
            Review review,
            Long viewerId
    ) {
        return viewerId != null
                && review.getUser().getId().equals(viewerId);
    }

    private void validateOwner(
            Review review,
            Long userId
    ) {
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "본인이 작성한 리뷰만 수정/삭제할 수 있습니다."
            );
        }
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 사용자입니다. id=" + userId
                        )
                );
    }

    private Restaurant findRestaurantOrThrow(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 식당입니다. id=" + restaurantId
                        )
                );
    }

    private Review findReviewOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 리뷰입니다. id=" + reviewId
                        )
                );
    }
}