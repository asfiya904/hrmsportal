package com.hireconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hireconnect.entity.Reimbursement;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {

    /**
     * Fetch reimbursements for an employee
     * Used in employee dashboard
     */
    List<Reimbursement> findByUserId(Long userId);

    /**
     * Fetch reimbursements by status
     * Used in admin approval screen
     */
    List<Reimbursement> findByStatus(Reimbursement.ReimbursementStatus status);

    /**
     * Count pending reimbursements (dashboard metric)
     * Enum-safe and production-ready
     */
    @Query("""
        SELECT COUNT(r)
        FROM Reimbursement r
        WHERE r.status = :status
    """)
    long countByStatus(
        @Param("status") Reimbursement.ReimbursementStatus status
    );

    /**
     * Admin view – latest reimbursements first
     */
    List<Reimbursement> findAllByOrderByCreatedAtDesc();
}
