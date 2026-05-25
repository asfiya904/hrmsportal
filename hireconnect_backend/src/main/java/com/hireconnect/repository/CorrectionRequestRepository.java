package com.hireconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hireconnect.entity.CorrectionRequest;

@Repository
public interface CorrectionRequestRepository
        extends JpaRepository<CorrectionRequest, Long> {

    /**
     * Fetch all correction requests raised by a user
     * Used for employee correction history
     */
    List<CorrectionRequest> findByUserId(Long userId);

    /**
     * Fetch correction requests by status
     * Used for admin pending / approved / rejected lists
     */
    List<CorrectionRequest> findByStatus(
            CorrectionRequest.CorrectionStatus status
    );

    /**
     * Dashboard metric: pending corrections count
     * Uses DB index on status
     */
    @Query("""
        SELECT COUNT(c)
        FROM CorrectionRequest c
        WHERE c.status = :status
    """)
    long countByStatus(
            @Param("status") CorrectionRequest.CorrectionStatus status
    );
}
