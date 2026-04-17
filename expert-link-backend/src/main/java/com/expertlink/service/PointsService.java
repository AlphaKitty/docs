package com.expertlink.service;

import com.expertlink.domain.PointsLedgerEntry;
import com.expertlink.domain.User;
import com.expertlink.dto.points.PointsLedgerEntryResponse;
import com.expertlink.repository.PointsLedgerEntryRepository;
import com.expertlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointsService {

    public static final String REASON_ENGAGEMENT_COMPLETE = "ENGAGEMENT_COMPLETE";

    private final UserRepository userRepository;
    private final PointsLedgerEntryRepository pointsLedgerEntryRepository;

    public Page<PointsLedgerEntryResponse> ledgerForUser(Long userId, Pageable pageable) {
        return pointsLedgerEntryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(e -> new PointsLedgerEntryResponse(
                        e.getId(),
                        e.getPointsDelta(),
                        e.getBalanceAfter(),
                        e.getReasonCode(),
                        e.getEngagementRequestId(),
                        e.getCreatedAt()
                ));
    }

    /**
     * 调⽤单结项后给专家 owner 入账（幂等由调用方保证只结项一次）。
     */
    @Transactional
    public void creditFromEngagement(Long userId, BigDecimal points, Long engagementRequestId) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("积分用户不存在: " + userId));
        BigDecimal cur = user.getPointsBalance() != null ? user.getPointsBalance() : BigDecimal.ZERO;
        BigDecimal next = cur.add(points).setScale(2, java.math.RoundingMode.HALF_UP);
        user.setPointsBalance(next);
        userRepository.save(user);
        PointsLedgerEntry entry = PointsLedgerEntry.builder()
                .userId(userId)
                .pointsDelta(points.setScale(2, java.math.RoundingMode.HALF_UP))
                .balanceAfter(next)
                .reasonCode(REASON_ENGAGEMENT_COMPLETE)
                .engagementRequestId(engagementRequestId)
                .build();
        pointsLedgerEntryRepository.save(entry);
    }
}
