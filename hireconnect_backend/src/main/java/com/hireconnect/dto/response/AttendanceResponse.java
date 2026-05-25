package com.hireconnect.dto.response;

import java.time.LocalDateTime;

/**
 * Production-ready attendance response DTO.
 * Frontend must ONLY display values from this object.
 * No calculations or inference on UI.
 */
public class AttendanceResponse {

    private Long id;

    private String status;

    private LocalDateTime shiftStartTime;
    private LocalDateTime shiftEndTime;

    private Integer totalSeconds;
    private Integer totalBreakSeconds;
    private Integer internalWorkSeconds;

    private LocalDateTime currentBreakStart;

    /**
     * Indicates whether timesheet is already submitted for this session.
     * Used only for UI enable/disable.
     */
    private Boolean timesheetSubmitted;

    /**
     * Optional informational message
     * (example: "Attendance already completed for today")
     */
    private String message;

    public AttendanceResponse() {
    }

    /* ================= GETTERS & SETTERS ================= */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(LocalDateTime shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public LocalDateTime getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(LocalDateTime shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public Integer getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(Integer totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public Integer getTotalBreakSeconds() {
        return totalBreakSeconds;
    }

    public void setTotalBreakSeconds(Integer totalBreakSeconds) {
        this.totalBreakSeconds = totalBreakSeconds;
    }

    public Integer getInternalWorkSeconds() {
        return internalWorkSeconds;
    }

    public void setInternalWorkSeconds(Integer internalWorkSeconds) {
        this.internalWorkSeconds = internalWorkSeconds;
    }

    public LocalDateTime getCurrentBreakStart() {
        return currentBreakStart;
    }

    public void setCurrentBreakStart(LocalDateTime currentBreakStart) {
        this.currentBreakStart = currentBreakStart;
    }

    public Boolean getTimesheetSubmitted() {
        return timesheetSubmitted;
    }

    public void setTimesheetSubmitted(Boolean timesheetSubmitted) {
        this.timesheetSubmitted = timesheetSubmitted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
