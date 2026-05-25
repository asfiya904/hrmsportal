package com.hireconnect.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Admin-only attendance request DTO.
 * Used for admin overrides, corrections, and manual attendance updates.
 */
public class AdminAttendanceRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Action is required")
    private AdminAttendanceAction action;

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;

    public AdminAttendanceRequest() {}

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AdminAttendanceAction getAction() {
        return action;
    }

    public void setAction(AdminAttendanceAction action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
