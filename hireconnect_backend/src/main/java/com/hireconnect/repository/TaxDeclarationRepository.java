package com.hireconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hireconnect.entity.TaxDeclaration;

@Repository
public interface TaxDeclarationRepository extends JpaRepository<TaxDeclaration, Long> {

    /**
     * Employee view – tax declarations of a user
     */
    List<TaxDeclaration> findByUserId(Long userId);

    /**
     * Admin filter – by status
     */
    List<TaxDeclaration> findByStatus(TaxDeclaration.TaxStatus status);

    /**
     * Admin dashboard – count by status
     */
    @Query("""
        SELECT COUNT(t)
        FROM TaxDeclaration t
        WHERE t.status = :status
    """)
    long countByStatus(
        @Param("status") TaxDeclaration.TaxStatus status
    );

    /**
     * Admin view – latest first
     */
    List<TaxDeclaration> findAllByOrderByCreatedAtDesc();
}
