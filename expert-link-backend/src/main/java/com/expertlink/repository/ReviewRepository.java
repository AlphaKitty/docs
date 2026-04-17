package com.expertlink.repository;

import com.expertlink.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByExpertId(Long expertId);
    
    List<Review> findByProjectId(Long projectId);
    
    List<Review> findByUserId(Long userId);
    
    Optional<Review> findByExpertIdAndUserId(Long expertId, Long userId);
    
    Optional<Review> findByProjectIdAndUserId(Long projectId, Long userId);
    
    List<Review> findByExpertIdAndProjectId(Long expertId, Long projectId);
    
    List<Review> findByRating(BigDecimal rating);
    
    List<Review> findByRatingGreaterThanEqual(BigDecimal minRating);
    
    List<Review> findByRatingLessThanEqual(BigDecimal maxRating);
    
    List<Review> findByRatingBetween(BigDecimal minRating, BigDecimal maxRating);
    
    List<Review> findByWouldRecommend(Boolean wouldRecommend);
    
    List<Review> findByStatus(String status);
    
    List<Review> findByIsVerified(Boolean isVerified);
    
    List<Review> findByIsAnonymous(Boolean isAnonymous);
    
    @Query("SELECT r FROM Review r WHERE " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.strengths) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Review> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE " +
           "r.expert.id = :expertId AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Review> searchByExpertIdAndKeyword(
            @Param("expertId") Long expertId,
            @Param("keyword") String keyword,
            Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE " +
           "r.project.id = :projectId AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Review> searchByProjectIdAndKeyword(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword,
            Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.expert.id = :expertId AND r.isVerified = true")
    BigDecimal calculateAverageRatingForExpert(@Param("expertId") Long expertId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.project.id = :projectId AND r.isVerified = true")
    BigDecimal calculateAverageRatingForProject(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.expert.id = :expertId AND r.isVerified = true")
    long countVerifiedReviewsForExpert(@Param("expertId") Long expertId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.project.id = :projectId AND r.isVerified = true")
    long countVerifiedReviewsForProject(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.expert.id = :expertId AND r.wouldRecommend = true")
    long countRecommendationsForExpert(@Param("expertId") Long expertId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.project.id = :projectId AND r.wouldRecommend = true")
    long countRecommendationsForProject(@Param("projectId") Long projectId);
    
    @Query("SELECT r FROM Review r WHERE " +
           "r.expert.id = :expertId AND " +
           "r.rating >= :minRating AND " +
           "r.status = 'PUBLISHED' " +
           "ORDER BY r.rating DESC")
    List<Review> findTopRatedReviewsForExpert(
            @Param("expertId") Long expertId,
            @Param("minRating") BigDecimal minRating,
            Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE " +
           "r.status IN :statuses " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByStatusIn(@Param("statuses") List<String> statuses, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE " +
           "r.expert.id = :expertId AND " +
           "r.status IN :statuses " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByExpertIdAndStatusIn(
            @Param("expertId") Long expertId,
            @Param("statuses") List<String> statuses,
            Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE " +
           "r.project.id = :projectId AND " +
           "r.status IN :statuses " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByProjectIdAndStatusIn(
            @Param("projectId") Long projectId,
            @Param("statuses") List<String> statuses,
            Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.helpfulCount >= :minHelpfulCount")
    List<Review> findByMinHelpfulCount(@Param("minHelpfulCount") Integer minHelpfulCount);
    
    @Query("SELECT r FROM Review r WHERE r.replyCount > 0")
    List<Review> findReviewsWithReplies();
    
    @Query("SELECT r FROM Review r WHERE r.status IN ('PUBLISHED', 'APPROVED')")
    Page<Review> findAllPublished(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.expert.id = :expertId AND r.status IN ('PUBLISHED', 'APPROVED')")
    Page<Review> findPublishedByExpertId(@Param("expertId") Long expertId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.project.id = :projectId AND r.status IN ('PUBLISHED', 'APPROVED')")
    Page<Review> findPublishedByProjectId(@Param("projectId") Long projectId, Pageable pageable);
    
    boolean existsByExpertIdAndUserId(Long expertId, Long userId);
    
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}