package com.expertlink.service;

import com.expertlink.domain.Review;
import com.expertlink.domain.Expert;
import com.expertlink.domain.Project;
import com.expertlink.domain.User;
import com.expertlink.domain.ProjectExpert;
import com.expertlink.repository.ReviewRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.ProjectRepository;
import com.expertlink.repository.UserRepository;
import com.expertlink.repository.ProjectExpertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ExpertRepository expertRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectExpertRepository projectExpertRepository;

    /**
     * 获取所有评价（分页）
     */
    public Page<Review> findAll(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    /**
     * 根据ID查找评价
     */
    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评价不存在，ID: " + id));
    }

    /**
     * 根据专家ID查找评价
     */
    public List<Review> findByExpertId(Long expertId) {
        return reviewRepository.findByExpertId(expertId);
    }

    /**
     * 根据项目ID查找评价
     */
    public List<Review> findByProjectId(Long projectId) {
        return reviewRepository.findByProjectId(projectId);
    }

    /**
     * 根据用户ID查找评价
     */
    public List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    /**
     * 根据专家ID和用户ID查找评价
     */
    public Optional<Review> findByExpertIdAndUserId(Long expertId, Long userId) {
        return reviewRepository.findByExpertIdAndUserId(expertId, userId);
    }

    /**
     * 根据项目ID和用户ID查找评价
     */
    public Optional<Review> findByProjectIdAndUserId(Long projectId, Long userId) {
        return reviewRepository.findByProjectIdAndUserId(projectId, userId);
    }

    /**
     * 根据专家ID和项目ID查找评价
     */
    public List<Review> findByExpertIdAndProjectId(Long expertId, Long projectId) {
        return reviewRepository.findByExpertIdAndProjectId(expertId, projectId);
    }

    /**
     * 创建评价
     */
    @Transactional
    public Review create(Review review, Long expertId, Long projectId, Long userId, Long projectExpertId) {
        // 验证专家存在
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("专家不存在，ID: " + expertId));
        review.setExpert(expert);

        // 验证项目存在（可选）
        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
            review.setProject(project);
        }

        // 验证用户存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));
        review.setUser(user);

        // 验证项目专家关联存在（可选）
        if (projectExpertId != null) {
            ProjectExpert projectExpert = projectExpertRepository.findById(projectExpertId)
                    .orElseThrow(() -> new RuntimeException("项目专家关联不存在，ID: " + projectExpertId));
            review.setProjectExpert(projectExpert);
        }

        // 设置默认值
        if (review.getStatus() == null) {
            review.setStatus("DRAFT");
        }
        if (review.getIsVerified() == null) {
            review.setIsVerified(false);
        }
        if (review.getIsAnonymous() == null) {
            review.setIsAnonymous(false);
        }
        if (review.getHelpfulCount() == null) {
            review.setHelpfulCount(0);
        }
        if (review.getReplyCount() == null) {
            review.setReplyCount(0);
        }
        
        // 验证评分范围
        if (review.getRating() != null) {
            if (review.getRating().compareTo(BigDecimal.ZERO) < 0 || review.getRating().compareTo(BigDecimal.valueOf(5)) > 0) {
                throw new RuntimeException("评分必须在0到5之间");
            }
        }
        
        // 验证子评分
        validateSubRatings(review);

        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        
        // 更新专家评分统计（可选，可以在审核通过后更新）
        // if ("PUBLISHED".equals(savedReview.getStatus()) && savedReview.getIsVerified()) {
        //     updateExpertRating(expertId, savedReview.getRating());
        // }
        
        return savedReview;
    }

    /**
     * 更新评价
     */
    @Transactional
    public Review update(Long id, Review reviewDetails) {
        Review existingReview = findById(id);
        
        // 检查是否允许编辑（例如草稿状态可以编辑，已发布状态不可编辑）
        if ("PUBLISHED".equals(existingReview.getStatus())) {
            throw new RuntimeException("已发布的评价不可编辑");
        }
        
        // 更新基本信息
        if (reviewDetails.getTitle() != null) {
            existingReview.setTitle(reviewDetails.getTitle());
        }
        if (reviewDetails.getComment() != null) {
            existingReview.setComment(reviewDetails.getComment());
        }
        if (reviewDetails.getStrengths() != null) {
            existingReview.setStrengths(reviewDetails.getStrengths());
        }
        if (reviewDetails.getAreasForImprovement() != null) {
            existingReview.setAreasForImprovement(reviewDetails.getAreasForImprovement());
        }
        if (reviewDetails.getWouldRecommend() != null) {
            existingReview.setWouldRecommend(reviewDetails.getWouldRecommend());
        }
        if (reviewDetails.getCommunicationRating() != null) {
            existingReview.setCommunicationRating(reviewDetails.getCommunicationRating());
        }
        if (reviewDetails.getTechnicalRating() != null) {
            existingReview.setTechnicalRating(reviewDetails.getTechnicalRating());
        }
        if (reviewDetails.getTimelinessRating() != null) {
            existingReview.setTimelinessRating(reviewDetails.getTimelinessRating());
        }
        if (reviewDetails.getOverallExperienceRating() != null) {
            existingReview.setOverallExperienceRating(reviewDetails.getOverallExperienceRating());
        }
        if (reviewDetails.getIsAnonymous() != null) {
            existingReview.setIsAnonymous(reviewDetails.getIsAnonymous());
        }
        if (reviewDetails.getStatus() != null) {
            existingReview.setStatus(reviewDetails.getStatus());
        }
        
        // 如果评分更新，需要验证
        if (reviewDetails.getRating() != null) {
            if (reviewDetails.getRating().compareTo(BigDecimal.ZERO) < 0 || 
                reviewDetails.getRating().compareTo(BigDecimal.valueOf(5)) > 0) {
                throw new RuntimeException("评分必须在0到5之间");
            }
            existingReview.setRating(reviewDetails.getRating());
        }
        
        // 验证子评分
        validateSubRatings(existingReview);

        existingReview.setUpdatedAt(LocalDateTime.now());

        return reviewRepository.save(existingReview);
    }

    /**
     * 删除评价
     */
    @Transactional
    public void delete(Long id) {
        Review review = findById(id);
        
        // 检查是否允许删除（例如草稿状态可以删除）
        if ("PUBLISHED".equals(review.getStatus())) {
            throw new RuntimeException("已发布的评价不可删除，请先标记为草稿");
        }
        
        reviewRepository.delete(review);
        log.info("删除评价，ID: {}", id);
    }

    /**
     * 发布评价（从草稿状态改为发布状态）
     */
    @Transactional
    public Review publishReview(Long id) {
        Review review = findById(id);
        
        if ("PUBLISHED".equals(review.getStatus())) {
            throw new RuntimeException("评价已经是发布状态");
        }
        
        // 验证必填字段
        if (review.getRating() == null) {
            throw new RuntimeException("发布评价必须包含评分");
        }
        if (review.getTitle() == null || review.getTitle().trim().isEmpty()) {
            throw new RuntimeException("发布评价必须包含标题");
        }
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            throw new RuntimeException("发布评价必须包含评论内容");
        }
        
        review.setStatus("PUBLISHED");
        review.setUpdatedAt(LocalDateTime.now());
        
        Review publishedReview = reviewRepository.save(review);
        
        // 发布后更新专家评分统计
        if (publishedReview.getExpert() != null && publishedReview.getIsVerified()) {
            updateExpertRating(publishedReview.getExpert().getId(), publishedReview.getRating());
        }
        
        return publishedReview;
    }

    /**
     * 标记评价为有用
     */
    @Transactional
    public Review markAsHelpful(Long id) {
        Review review = findById(id);
        review.incrementHelpfulCount();
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    /**
     * 回复评价
     */
    @Transactional
    public Review addReply(Long id) {
        Review review = findById(id);
        // 这里只是增加回复计数，实际回复内容可能在另一个系统处理
        review.setReplyCount(review.getReplyCount() + 1);
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    /**
     * 验证评价
     */
    @Transactional
    public Review verifyReview(Long id) {
        Review review = findById(id);
        review.setIsVerified(true);
        review.setUpdatedAt(LocalDateTime.now());
        
        // 如果评价是发布状态且已验证，更新专家评分
        if ("PUBLISHED".equals(review.getStatus()) && review.getExpert() != null) {
            updateExpertRating(review.getExpert().getId(), review.getRating());
        }
        
        return reviewRepository.save(review);
    }

    /**
     * 搜索评价
     */
    public Page<Review> searchByKeyword(String keyword, Pageable pageable) {
        return reviewRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 根据专家ID和关键词搜索评价
     */
    public Page<Review> searchByExpertIdAndKeyword(Long expertId, String keyword, Pageable pageable) {
        return reviewRepository.searchByExpertIdAndKeyword(expertId, keyword, pageable);
    }

    /**
     * 根据项目ID和关键词搜索评价
     */
    public Page<Review> searchByProjectIdAndKeyword(Long projectId, String keyword, Pageable pageable) {
        return reviewRepository.searchByProjectIdAndKeyword(projectId, keyword, pageable);
    }

    /**
     * 获取专家的平均评分
     */
    public BigDecimal calculateAverageRatingForExpert(Long expertId) {
        return reviewRepository.calculateAverageRatingForExpert(expertId);
    }

    /**
     * 获取项目的平均评分
     */
    public BigDecimal calculateAverageRatingForProject(Long projectId) {
        return reviewRepository.calculateAverageRatingForProject(projectId);
    }

    /**
     * 获取专家的已验证评价数量
     */
    public long countVerifiedReviewsForExpert(Long expertId) {
        return reviewRepository.countVerifiedReviewsForExpert(expertId);
    }

    /**
     * 获取项目的已验证评价数量
     */
    public long countVerifiedReviewsForProject(Long projectId) {
        return reviewRepository.countVerifiedReviewsForProject(projectId);
    }

    /**
     * 获取专家的推荐数量
     */
    public long countRecommendationsForExpert(Long expertId) {
        return reviewRepository.countRecommendationsForExpert(expertId);
    }

    /**
     * 获取项目的推荐数量
     */
    public long countRecommendationsForProject(Long projectId) {
        return reviewRepository.countRecommendationsForProject(projectId);
    }

    /**
     * 获取评分高于阈值的评价
     */
    public List<Review> findTopRatedReviewsForExpert(Long expertId, BigDecimal minRating, Pageable pageable) {
        return reviewRepository.findTopRatedReviewsForExpert(expertId, minRating, pageable);
    }

    /**
     * 根据状态列表查找评价
     */
    public Page<Review> findByStatusIn(List<String> statuses, Pageable pageable) {
        return reviewRepository.findByStatusIn(statuses, pageable);
    }

    /**
     * 根据专家ID和状态列表查找评价
     */
    public Page<Review> findByExpertIdAndStatusIn(Long expertId, List<String> statuses, Pageable pageable) {
        return reviewRepository.findByExpertIdAndStatusIn(expertId, statuses, pageable);
    }

    /**
     * 根据项目ID和状态列表查找评价
     */
    public Page<Review> findByProjectIdAndStatusIn(Long projectId, List<String> statuses, Pageable pageable) {
        return reviewRepository.findByProjectIdAndStatusIn(projectId, statuses, pageable);
    }

    /**
     * 获取所有已发布的评价
     */
    public Page<Review> findAllPublished(Pageable pageable) {
        return reviewRepository.findAllPublished(pageable);
    }

    /**
     * 获取专家的已发布评价
     */
    public Page<Review> findPublishedByExpertId(Long expertId, Pageable pageable) {
        return reviewRepository.findPublishedByExpertId(expertId, pageable);
    }

    /**
     * 获取项目的已发布评价
     */
    public Page<Review> findPublishedByProjectId(Long projectId, Pageable pageable) {
        return reviewRepository.findPublishedByProjectId(projectId, pageable);
    }

    /**
     * 检查用户是否已评价专家
     */
    public boolean hasUserReviewedExpert(Long expertId, Long userId) {
        return reviewRepository.existsByExpertIdAndUserId(expertId, userId);
    }

    /**
     * 检查用户是否已评价项目
     */
    public boolean hasUserReviewedProject(Long projectId, Long userId) {
        return reviewRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    /**
     * 更新专家评分统计（私有方法）
     */
    @Transactional
    private void updateExpertRating(Long expertId, BigDecimal rating) {
        // 这里可以调用ExpertService的updateRating方法
        // 或者直接操作专家实体更新评分
        // 由于循环依赖问题，可以考虑使用事件机制或直接在Service中调用ExpertRepository
        // 简化处理：这里只记录日志
        log.info("专家ID {} 收到新评分: {}", expertId, rating);
        
        // 实际应用中应该调用ExpertService.updateRating(expertId, rating)
        // 但为避免循环依赖，可以考虑使用异步事件或直接调用Repository
    }

    /**
     * 验证子评分（私有方法）
     */
    private void validateSubRatings(Review review) {
        BigDecimal[] subRatings = {
            review.getCommunicationRating(),
            review.getTechnicalRating(),
            review.getTimelinessRating(),
            review.getOverallExperienceRating()
        };
        
        for (BigDecimal subRating : subRatings) {
            if (subRating != null && 
                (subRating.compareTo(BigDecimal.ZERO) < 0 || subRating.compareTo(BigDecimal.valueOf(5)) > 0)) {
                throw new RuntimeException("子评分必须在0到5之间");
            }
        }
    }
}