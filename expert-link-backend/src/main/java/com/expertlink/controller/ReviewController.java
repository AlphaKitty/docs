package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.domain.Review;
import com.expertlink.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 获取所有评价（分页）
     * GET /api/reviews
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> getAllReviews(Pageable pageable) {
        Page<Review> reviews = reviewService.findAll(pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 根据ID获取评价
     * GET /api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Review>> getReviewById(@PathVariable Long id) {
        Review review = reviewService.findById(id);
        return ApiResponses.ok(review);
    }

    /**
     * 根据专家ID获取评价
     * GET /api/reviews/expert/{expertId}
     */
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByExpertId(@PathVariable Long expertId) {
        List<Review> reviews = reviewService.findByExpertId(expertId);
        return ApiResponses.ok(reviews);
    }

    /**
     * 根据项目ID获取评价
     * GET /api/reviews/project/{projectId}
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByProjectId(@PathVariable Long projectId) {
        List<Review> reviews = reviewService.findByProjectId(projectId);
        return ApiResponses.ok(reviews);
    }

    /**
     * 根据用户ID获取评价
     * GET /api/reviews/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByUserId(@PathVariable Long userId) {
        List<Review> reviews = reviewService.findByUserId(userId);
        return ApiResponses.ok(reviews);
    }

    /**
     * 根据专家ID和用户ID获取评价
     * GET /api/reviews/expert/{expertId}/user/{userId}
     */
    @GetMapping("/expert/{expertId}/user/{userId}")
    public ResponseEntity<ApiResponse<Review>> getReviewByExpertIdAndUserId(
            @PathVariable Long expertId,
            @PathVariable Long userId) {
        Optional<Review> review = reviewService.findByExpertIdAndUserId(expertId, userId);
        return review.map(ApiResponses::ok)
                .orElseGet(() -> ApiResponses.notFound("评价不存在"));
    }

    /**
     * 根据项目ID和用户ID获取评价
     * GET /api/reviews/project/{projectId}/user/{userId}
     */
    @GetMapping("/project/{projectId}/user/{userId}")
    public ResponseEntity<ApiResponse<Review>> getReviewByProjectIdAndUserId(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        Optional<Review> review = reviewService.findByProjectIdAndUserId(projectId, userId);
        return review.map(ApiResponses::ok)
                .orElseGet(() -> ApiResponses.notFound("评价不存在"));
    }

    /**
     * 根据专家ID和项目ID获取评价
     * GET /api/reviews/expert/{expertId}/project/{projectId}
     */
    @GetMapping("/expert/{expertId}/project/{projectId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByExpertIdAndProjectId(
            @PathVariable Long expertId,
            @PathVariable Long projectId) {
        List<Review> reviews = reviewService.findByExpertIdAndProjectId(expertId, projectId);
        return ApiResponses.ok(reviews);
    }

    /**
     * 搜索评价
     * GET /api/reviews/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> searchReviews(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Review> reviews = reviewService.searchByKeyword(keyword, pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 根据专家ID和关键词搜索评价
     * GET /api/reviews/expert/{expertId}/search
     */
    @GetMapping("/expert/{expertId}/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> searchReviewsByExpertId(
            @PathVariable Long expertId,
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Review> reviews = reviewService.searchByExpertIdAndKeyword(expertId, keyword, pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 根据项目ID和关键词搜索评价
     * GET /api/reviews/project/{projectId}/search
     */
    @GetMapping("/project/{projectId}/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> searchReviewsByProjectId(
            @PathVariable Long projectId,
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Review> reviews = reviewService.searchByProjectIdAndKeyword(projectId, keyword, pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 获取专家的平均评分
     * GET /api/reviews/expert/{expertId}/average-rating
     */
    @GetMapping("/expert/{expertId}/average-rating")
    public ResponseEntity<ApiResponse<BigDecimal>> getAverageRatingForExpert(@PathVariable Long expertId) {
        BigDecimal averageRating = reviewService.calculateAverageRatingForExpert(expertId);
        return ApiResponses.ok(averageRating);
    }

    /**
     * 获取项目的平均评分
     * GET /api/reviews/project/{projectId}/average-rating
     */
    @GetMapping("/project/{projectId}/average-rating")
    public ResponseEntity<ApiResponse<BigDecimal>> getAverageRatingForProject(@PathVariable Long projectId) {
        BigDecimal averageRating = reviewService.calculateAverageRatingForProject(projectId);
        return ApiResponses.ok(averageRating);
    }

    /**
     * 获取专家的已验证评价数量
     * GET /api/reviews/expert/{expertId}/verified-count
     */
    @GetMapping("/expert/{expertId}/verified-count")
    public ResponseEntity<ApiResponse<Long>> getVerifiedReviewsCountForExpert(@PathVariable Long expertId) {
        long count = reviewService.countVerifiedReviewsForExpert(expertId);
        return ApiResponses.ok(count);
    }

    /**
     * 获取项目的已验证评价数量
     * GET /api/reviews/project/{projectId}/verified-count
     */
    @GetMapping("/project/{projectId}/verified-count")
    public ResponseEntity<ApiResponse<Long>> getVerifiedReviewsCountForProject(@PathVariable Long projectId) {
        long count = reviewService.countVerifiedReviewsForProject(projectId);
        return ApiResponses.ok(count);
    }

    /**
     * 获取专家的推荐数量
     * GET /api/reviews/expert/{expertId}/recommendation-count
     */
    @GetMapping("/expert/{expertId}/recommendation-count")
    public ResponseEntity<ApiResponse<Long>> getRecommendationCountForExpert(@PathVariable Long expertId) {
        long count = reviewService.countRecommendationsForExpert(expertId);
        return ApiResponses.ok(count);
    }

    /**
     * 获取项目的推荐数量
     * GET /api/reviews/project/{projectId}/recommendation-count
     */
    @GetMapping("/project/{projectId}/recommendation-count")
    public ResponseEntity<ApiResponse<Long>> getRecommendationCountForProject(@PathVariable Long projectId) {
        long count = reviewService.countRecommendationsForProject(projectId);
        return ApiResponses.ok(count);
    }

    /**
     * 获取专家的高评分评价
     * GET /api/reviews/expert/{expertId}/top-rated
     */
    @GetMapping("/expert/{expertId}/top-rated")
    public ResponseEntity<ApiResponse<List<Review>>> getTopRatedReviewsForExpert(
            @PathVariable Long expertId,
            @RequestParam BigDecimal minRating,
            Pageable pageable) {
        List<Review> reviews = reviewService.findTopRatedReviewsForExpert(expertId, minRating, pageable);
        return ApiResponses.ok(reviews);
    }

    /**
     * 根据状态列表获取评价
     * POST /api/reviews/by-statuses
     */
    @PostMapping("/by-statuses")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> getReviewsByStatuses(
            @RequestBody List<String> statuses,
            Pageable pageable) {
        Page<Review> reviews = reviewService.findByStatusIn(statuses, pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 根据专家ID和状态列表获取评价
     * POST /api/reviews/expert/{expertId}/by-statuses
     */
    @PostMapping("/expert/{expertId}/by-statuses")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> getReviewsByExpertIdAndStatuses(
            @PathVariable Long expertId,
            @RequestBody List<String> statuses,
            Pageable pageable) {
        Page<Review> reviews = reviewService.findByExpertIdAndStatusIn(expertId, statuses, pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 根据项目ID和状态列表获取评价
     * POST /api/reviews/project/{projectId}/by-statuses
     */
    @PostMapping("/project/{projectId}/by-statuses")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> getReviewsByProjectIdAndStatuses(
            @PathVariable Long projectId,
            @RequestBody List<String> statuses,
            Pageable pageable) {
        Page<Review> reviews = reviewService.findByProjectIdAndStatusIn(projectId, statuses, pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 获取所有已发布的评价
     * GET /api/reviews/published
     */
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> getAllPublishedReviews(Pageable pageable) {
        Page<Review> reviews = reviewService.findAllPublished(pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 获取专家的已发布评价
     * GET /api/reviews/expert/{expertId}/published
     */
    @GetMapping("/expert/{expertId}/published")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> getPublishedReviewsByExpertId(
            @PathVariable Long expertId,
            Pageable pageable) {
        Page<Review> reviews = reviewService.findPublishedByExpertId(expertId, pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 获取项目的已发布评价
     * GET /api/reviews/project/{projectId}/published
     */
    @GetMapping("/project/{projectId}/published")
    public ResponseEntity<ApiResponse<PaginatedResponse<Review>>> getPublishedReviewsByProjectId(
            @PathVariable Long projectId,
            Pageable pageable) {
        Page<Review> reviews = reviewService.findPublishedByProjectId(projectId, pageable);
        return ApiResponses.okPage(reviews);
    }

    /**
     * 检查用户是否已评价专家
     * GET /api/reviews/check/expert/{expertId}/user/{userId}
     */
    @GetMapping("/check/expert/{expertId}/user/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkUserHasReviewedExpert(
            @PathVariable Long expertId,
            @PathVariable Long userId) {
        boolean hasReviewed = reviewService.hasUserReviewedExpert(expertId, userId);
        return ApiResponses.ok(hasReviewed);
    }

    /**
     * 检查用户是否已评价项目
     * GET /api/reviews/check/project/{projectId}/user/{userId}
     */
    @GetMapping("/check/project/{projectId}/user/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkUserHasReviewedProject(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        boolean hasReviewed = reviewService.hasUserReviewedProject(projectId, userId);
        return ApiResponses.ok(hasReviewed);
    }

    /**
     * 创建评价
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(
            @RequestBody Review review,
            @RequestParam Long expertId,
            @RequestParam(required = false) Long projectId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long projectExpertId) {
        Review createdReview = reviewService.create(review, expertId, projectId, userId, projectExpertId);
        return ApiResponses.created(createdReview);
    }

    /**
     * 更新评价
     * PUT /api/reviews/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Review>> updateReview(
            @PathVariable Long id,
            @RequestBody Review reviewDetails) {
        Review updatedReview = reviewService.update(id, reviewDetails);
        return ApiResponses.ok(updatedReview);
    }

    /**
     * 删除评价
     * DELETE /api/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return ApiResponses.noContent("评价删除成功");
    }

    /**
     * 发布评价
     * PUT /api/reviews/{id}/publish
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Review>> publishReview(@PathVariable Long id) {
        Review publishedReview = reviewService.publishReview(id);
        return ApiResponses.ok(publishedReview);
    }

    /**
     * 标记评价为有用
     * PUT /api/reviews/{id}/mark-helpful
     */
    @PutMapping("/{id}/mark-helpful")
    public ResponseEntity<ApiResponse<Review>> markReviewAsHelpful(@PathVariable Long id) {
        Review updatedReview = reviewService.markAsHelpful(id);
        return ApiResponses.ok(updatedReview);
    }

    /**
     * 增加回复计数
     * PUT /api/reviews/{id}/add-reply
     */
    @PutMapping("/{id}/add-reply")
    public ResponseEntity<ApiResponse<Review>> addReplyToReview(@PathVariable Long id) {
        Review updatedReview = reviewService.addReply(id);
        return ApiResponses.ok(updatedReview);
    }

    /**
     * 验证评价
     * PUT /api/reviews/{id}/verify
     */
    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Review>> verifyReview(@PathVariable Long id) {
        Review verifiedReview = reviewService.verifyReview(id);
        return ApiResponses.ok(verifiedReview);
    }
}