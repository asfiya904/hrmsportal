package com.hireconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hireconnect.entity.OnboardingAnswer;

@Repository
public interface OnboardingAnswerRepository extends JpaRepository<OnboardingAnswer, Long> {

    /**
     * Fetch all onboarding answers for a user
     * Used for:
     * - onboarding review
     * - admin audit
     */
    List<OnboardingAnswer> findByUserId(Long userId);

    /**
     * Fetch answer for a specific onboarding step
     * Used for:
     * - step-wise onboarding flow
     */
    Optional<OnboardingAnswer> findByUserIdAndStep(Long userId, Integer step);

    /**
     * Fetch latest submitted onboarding answer
     * Used for:
     * - resume onboarding
     * - onboarding progress check
     */
    @Query("""
        SELECT o
        FROM OnboardingAnswer o
        WHERE o.userId = :userId
        ORDER BY o.submittedAt DESC
    """)
    Optional<OnboardingAnswer> findLatestByUserId(
        @Param("userId") Long userId
    );
}
