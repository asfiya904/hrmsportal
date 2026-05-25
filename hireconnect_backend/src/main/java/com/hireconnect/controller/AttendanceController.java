package com.hireconnect.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.dto.request.AttendanceCorrectionRequest;
import com.hireconnect.dto.request.LeaveRequest;
import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.dto.response.AttendanceResponse;
import com.hireconnect.entity.AttendanceSession;
import com.hireconnect.service.AttendanceService;
import com.hireconnect.util.SecurityUtil;

import jakarta.validation.Valid;

/**
 * Production-grade Attendance Controller.
 * Backend is the single source of truth.
 * Frontend only consumes responses.
 */
@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:3000"},
        allowCredentials = "true"
)
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /* ================= PUNCH ACTIONS ================= */

    @PostMapping("/start-work")
    public ResponseEntity<ApiResponse<AttendanceResponse>> startWork() {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        AttendanceResponse response =
                attendanceService.startWorkAndBuildResponse(employeeId);

        return ResponseEntity.ok(ApiResponse.success("Work session started", response));
    }


    @PostMapping("/break-start")
    public ResponseEntity<ApiResponse<AttendanceResponse>> startBreak() {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        AttendanceResponse response =
                attendanceService.startBreakAndBuildResponse(employeeId);

        return ResponseEntity.ok(ApiResponse.success("Break started", response));
    }

    @PostMapping("/break-resume")
    public ResponseEntity<ApiResponse<AttendanceResponse>> resumeBreak() {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        AttendanceResponse response =
                attendanceService.resumeBreakAndBuildResponse(employeeId);

        return ResponseEntity.ok(ApiResponse.success("Work resumed", response));
    }
    
    @PostMapping("/end-work")
    public ResponseEntity<ApiResponse<AttendanceResponse>> endWork() {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        AttendanceResponse response =
                attendanceService.endWorkAndBuildResponse(employeeId);

        return ResponseEntity.ok(ApiResponse.success("Work session ended", response));
    }

    /* ================= TIMESHEET ================= */

    @PostMapping("/save-timesheet")
    public ResponseEntity<ApiResponse<String>> saveTimesheet() {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();
        attendanceService.saveTimesheet(employeeId);

        return ResponseEntity.ok(ApiResponse.success("Timesheet submitted", null));
    }
    /* ================= TODAY ================= */

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getTodayAttendance() {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        AttendanceResponse response =
                attendanceService.getTodayAttendance(employeeId);

        return ResponseEntity.ok(ApiResponse.success("Today's attendance", response));
    }

    /* ================= CALENDAR ================= */

    @GetMapping("/calendar/{year}/{month}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getMyCalendarData(
            @PathVariable Integer year,
            @PathVariable Integer month) {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        Map<String, String> calendarData =
                attendanceService.getCalendarData(employeeId, year, month);

        return ResponseEntity.ok(
                ApiResponse.success("Calendar data fetched", calendarData)
        );
    }


    /* ================= MONTHLY / SUMMARY ================= */

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyMonthlyAttendance(
            @RequestParam Integer year,
            @RequestParam Integer month) {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        Map<String, Object> monthlyData =
                attendanceService.getMonthlyAttendance(employeeId, year, month);

        return ResponseEntity.ok(
                ApiResponse.success("Monthly attendance fetched", monthlyData)
        );
    }



    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyAttendanceSummary() {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        Map<String, Object> summary =
                attendanceService.getAttendanceSummary(employeeId);

        return ResponseEntity.ok(
                ApiResponse.success("Attendance summary fetched", summary)
        );
    }


    /* ================= LEAVE / CORRECTION ================= */

    @PostMapping("/leave-request")
    public ResponseEntity<ApiResponse<String>> submitLeaveRequest(
            @Valid @RequestBody LeaveRequest request) {

        attendanceService.submitLeaveRequest(request);

        return ResponseEntity.ok(
                ApiResponse.success("Leave request submitted successfully", null)
        );
    }

    @PostMapping("/attendance/correction-request")
    public ResponseEntity<ApiResponse<String>> submitCorrectionRequest(
            @Valid @RequestBody AttendanceCorrectionRequest request
    ) {
        attendanceService.submitCorrectionRequest(request);
        return ResponseEntity.ok(
            ApiResponse.success("Correction request submitted successfully")
        );
    }


    /* ================= HISTORY ================= */

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AttendanceSession>>> getMyAttendanceHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Long employeeId = SecurityUtil.getLoggedInEmployeeId();

        LocalDateTime start;
        LocalDateTime end;

        if (startDate != null && endDate != null) {
            start = LocalDate.parse(startDate).atStartOfDay();
            end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        } else {
            // default: last 30 days
            end = LocalDateTime.now();
            start = end.minusDays(30);
        }

        List<AttendanceSession> history =
                attendanceService.getAttendanceHistory(employeeId, start, end);

        return ResponseEntity.ok(
                ApiResponse.success("Attendance history fetched", history)
        );
    }



}
