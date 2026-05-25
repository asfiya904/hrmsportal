package com.hireconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hireconnect.entity.AttendanceSession;

@Repository
public interface AttendanceSessionRepository
        extends JpaRepository<AttendanceSession, Long> {

    /* ================= ACTIVE SESSION (LOCKED – PRODUCTION SAFE) ================= */

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM AttendanceSession a
        WHERE a.employeeId = :employeeId
          AND a.endTime IS NULL
    """)
    Optional<AttendanceSession> findActiveSessionForUpdate(
            @Param("employeeId") Long employeeId
    );

    /* ================= READ-ONLY ACTIVE SESSION ================= */

    @Query("""
        SELECT a FROM AttendanceSession a
        WHERE a.employeeId = :employeeId
          AND a.endTime IS NULL
        ORDER BY a.startTime DESC
    """)
    Optional<AttendanceSession> findActiveSessionByEmployeeId(
            @Param("employeeId") Long employeeId
    );

    /* ================= PREVENT MULTIPLE SESSIONS (LOGICAL CHECK) ================= */

    boolean existsByEmployeeIdAndStartTimeBetween(
            Long employeeId,
            LocalDateTime dayStart,
            LocalDateTime dayEnd
    );

    /* ================= SESSIONS FOR A DAY ================= */

    @Query("""
        SELECT a FROM AttendanceSession a
        WHERE a.employeeId = :employeeId
          AND a.startTime >= :dayStart
          AND a.startTime < :dayEnd
        ORDER BY a.startTime ASC
    """)
    List<AttendanceSession> findSessionsForDay(
            @Param("employeeId") Long employeeId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    /* ================= TODAY (LATEST SESSION) ================= */

    @Query("""
        SELECT a FROM AttendanceSession a
        WHERE a.employeeId = :employeeId
          AND a.startTime >= :dayStart
          AND a.startTime < :dayEnd
        ORDER BY a.startTime DESC
    """)
    Optional<AttendanceSession> findTodayLatestSession(
            @Param("employeeId") Long employeeId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    /* ================= DATE RANGE ================= */

    @Query("""
        SELECT a FROM AttendanceSession a
        WHERE a.employeeId = :employeeId
          AND a.startTime >= :startDateTime
          AND a.startTime < :endDateTime
        ORDER BY a.startTime DESC
    """)
    List<AttendanceSession> findByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /* ================= ADMIN / AUTO CLOSE ================= */

    @Query("""
        SELECT a FROM AttendanceSession a
        WHERE a.endTime IS NULL
    """)
    List<AttendanceSession> findAllActiveSessions();

    /* ================= DASHBOARD / STATS ================= */

    @Query("""
        SELECT a FROM AttendanceSession a
        WHERE a.startTime >= :todayStart
          AND a.startTime < :todayEnd
    """)
    List<AttendanceSession> findTodaysSessions(
            @Param("todayStart") LocalDateTime todayStart,
            @Param("todayEnd") LocalDateTime todayEnd
    );

    @Query("""
        SELECT COUNT(DISTINCT a.employeeId)
        FROM AttendanceSession a
        WHERE a.startTime >= :todayStart
          AND a.startTime < :todayEnd
    """)
    long countPresentToday(
            @Param("todayStart") LocalDateTime todayStart,
            @Param("todayEnd") LocalDateTime todayEnd
    );

    @Query("""
        SELECT COUNT(DISTINCT a.employeeId)
        FROM AttendanceSession a
        WHERE a.startTime >= :todayStart
          AND a.startTime < :todayEnd
          AND a.status = :status
    """)
    long countByStatusToday(
            @Param("status") AttendanceSession.AttendanceStatus status,
            @Param("todayStart") LocalDateTime todayStart,
            @Param("todayEnd") LocalDateTime todayEnd
    );
}
