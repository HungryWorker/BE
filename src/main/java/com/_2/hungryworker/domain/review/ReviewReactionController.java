package com._2.hungryworker.domain.review;

import com._2.hungryworker.domain.review.dto.ReviewReactionRequest;
import com._2.hungryworker.domain.review.dto.ReviewReactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewReactionController {

    private final ReviewReactionService reviewReactionService;

    public ReviewReactionController(ReviewReactionService reviewReactionService) {
        this.reviewReactionService = reviewReactionService;
    }

    @PutMapping("/api/reviews/{reviewId}/reaction")
    public ResponseEntity<ReviewReactionResponse> react(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewReactionRequest request
    ) {
        return ResponseEntity.ok(
                reviewReactionService.react(userId, reviewId, request)
        );
    }
}