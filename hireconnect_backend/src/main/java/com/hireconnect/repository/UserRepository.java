package com.hireconnect.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.hireconnect.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /* ================= BASIC ================= */

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByEmployeeId(String employeeId);

    /* ================= ROLE / STATUS ================= */

    List<User> findByRole(User.Role role);

    List<User> findByRoleAndDeletedAtIsNull(User.Role role);

    List<User> findByRoleAndStatus(User.Role role, User.Status status);

    List<User> findByRoleAndStatusAndDeletedAtIsNull(User.Role role, User.Status status);

    List<User> findByStatus(User.Status status);

    List<User> findByStatusAndDeletedAtIsNull(User.Status status);
  

    /* ================= APPROVAL ================= */

    List<User> findByApproved(Boolean approved);

    List<User> findByApprovedAndDeletedAtIsNull(Boolean approved);

    List<User> findByApprovedAndRole(Boolean approved, User.Role role);

    /* ================= ONBOARDING ================= */

    List<User> findByOnboardingStatus(User.OnboardingStatus status);

    List<User> findByOnboardingStatusAndRole(
            User.OnboardingStatus status,
            User.Role role
    );

    /* ================= DEPARTMENT ================= */

    List<User> findByDepartment(String department);

    List<User> findByDepartmentAndDeletedAtIsNull(String department);


    /* ================= COUNTS ================= */

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.status = :status")
    long countByRoleAndStatus(
            @Param("role") User.Role role,
            @Param("status") User.Status status
    );

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    long countActiveByRole(@Param("role") User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.approved = :approved AND u.role = :role")
    long countByApprovedAndRole(
            @Param("approved") Boolean approved,
            @Param("role") User.Role role
    );
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.Status status);


    /* ================= SEARCH ================= */

    @Query("""
        SELECT u FROM User u
        WHERE u.deletedAt IS NULL
          AND (
                LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(u.mobile)   LIKE LOWER(CONCAT('%', :query, '%'))
          )
    """)
    List<User> searchUsers(@Param("query") String query);

    @Query("""
        SELECT u FROM User u
        WHERE u.role = :role
          AND u.deletedAt IS NULL
          AND (
                LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :query, '%'))
          )
    """)
    List<User> searchUsersByRole(
            @Param("role") User.Role role,
            @Param("query") String query
    );

    /* ================= LOGIN ================= */

    @Query("""
        SELECT u FROM User u
        WHERE u.lastLoginAt >= :since
          AND u.deletedAt IS NULL
        ORDER BY u.lastLoginAt DESC
    """)
    List<User> findRecentlyLoggedIn(@Param("since") LocalDateTime since);

    /* ================= TOKENS ================= */

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    Optional<User> findByResetPasswordTokenAndResetPasswordExpireAfter(
            String token,
            LocalDateTime now
    );

    /* ================= SOFT DELETE ================= */

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL ORDER BY u.deletedAt DESC")
    List<User> findAllDeleted();

    /* ================= UPCOMING BIRTHDAYS ================= */

    @Query("""
        SELECT u FROM User u
        WHERE u.deletedAt IS NULL
          AND u.dob IS NOT NULL
          AND (
                (MONTH(u.dob) = MONTH(:start) AND DAY(u.dob) >= DAY(:start))
             OR (MONTH(u.dob) = MONTH(:end)   AND DAY(u.dob) <= DAY(:end))
             OR (MONTH(:start) <> MONTH(:end)
                 AND MONTH(u.dob) BETWEEN MONTH(:start) + 1 AND MONTH(:end) - 1)
          )
    """)
    List<User> findUpcomingBirthdays(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    /* ================= UPCOMING ANNIVERSARIES ================= */

    @Query("""
        SELECT u FROM User u
        WHERE u.deletedAt IS NULL
          AND u.joiningDate IS NOT NULL
          AND (
                (MONTH(u.joiningDate) = MONTH(:start) AND DAY(u.joiningDate) >= DAY(:start))
             OR (MONTH(u.joiningDate) = MONTH(:end)   AND DAY(u.joiningDate) <= DAY(:end))
             OR (MONTH(:start) <> MONTH(:end)
                 AND MONTH(u.joiningDate) BETWEEN MONTH(:start) + 1 AND MONTH(:end) - 1)
          )
    """)
    List<User> findUpcomingAnniversaries(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
