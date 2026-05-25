package com.hireconnect.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_seconds", nullable = false)
    private Integer totalSeconds = 0;

    @Column(name = "total_break_seconds", nullable = false)
    private Integer totalBreakSeconds = 0;

    @Column(name = "internal_work_seconds", nullable = false)
    private Integer internalWorkSeconds = 0;

    @Column(name = "last_break_start")
    private LocalDateTime lastBreakStart;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.WORKING;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AttendanceStatus {
        WORKING,
        ON_BREAK,
        COMPLETED,
        HALF_DAY,
        PARTIAL,
        ABSENT,
        WEEK_OFF,
        HOLIDAY,
        LEAVE,
        COMPENSATION_OFF,
        SATURDAY_WORK,
        SUNDAY_WORK
    }

    public AttendanceSession() {
        this.totalSeconds = 0;
        this.totalBreakSeconds = 0;
        this.internalWorkSeconds = 0;
        this.status = AttendanceStatus.WORKING;
    }

    public AttendanceSession(Long employeeId) {
        this.employeeId = employeeId;
        this.totalSeconds = 0;
        this.totalBreakSeconds = 0;
        this.internalWorkSeconds = 0;
        this.status = AttendanceStatus.WORKING;
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(Integer totalSeconds) {
        this.totalSeconds = totalSeconds != null ? totalSeconds : 0;
    }

    public Integer getTotalBreakSeconds() {
        return totalBreakSeconds;
    }

    public void setTotalBreakSeconds(Integer totalBreakSeconds) {
        this.totalBreakSeconds = totalBreakSeconds != null ? totalBreakSeconds : 0;
    }

    public Integer getInternalWorkSeconds() {
        return internalWorkSeconds;
    }

    public void setInternalWorkSeconds(Integer internalWorkSeconds) {
        this.internalWorkSeconds = internalWorkSeconds != null ? internalWorkSeconds : 0;
    }

    public LocalDateTime getLastBreakStart() {
        return lastBreakStart;
    }

    public void setLastBreakStart(LocalDateTime lastBreakStart) {
        this.lastBreakStart = lastBreakStart;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status != null ? status : AttendanceStatus.WORKING;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
