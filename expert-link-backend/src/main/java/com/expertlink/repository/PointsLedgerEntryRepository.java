package com.expertlink.repository;

import com.expertlink.domain.PointsLedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsLedgerEntryRepository extends JpaRepository<PointsLedgerEntry, Long> {

    Page<PointsLedgerEntry> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
