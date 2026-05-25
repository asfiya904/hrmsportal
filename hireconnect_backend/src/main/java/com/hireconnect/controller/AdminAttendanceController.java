package com.hireconnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.dto.request.ApplyLeaveRequest;
import com.hireconnect.dto.request.AdminAttendanceRequest;
import com.hireconnect.dto.request.BulkActionRequest;
import com.hireconnect.dto.request.HrAttendanceRequest;
import com.hireconnect.dto.request.ManualAttendanceRequest;
import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.dto.response.DashboardStatsResponse;
import com.hireconnect.service.AdminAttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:3000"},
	    allowCredentials = "true"
	)
public class AdminAttendanceController {

    private final AdminAttendanceService adminAttendanceService;

    /* ================= LIVE / DASHBOARD ================= */

    @GetMapping("/live")
    public ResponseEntity<ApiResponse<?>> getLiveAttendance() {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Live attendance fetched",
                            adminAttendanceService.getLiveAttendance()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        try {
            DashboardStatsResponse stats = adminAttendanceService.getDashboardStats();
            return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /* ================= REPORTS / TIMESHEETS ================= */

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<?>> getReports(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Reports fetched",
                            adminAttendanceService.getReports(month, year)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/timesheets")
    public ResponseEntity<ApiResponse<?>> getTimesheets() {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Timesheets fetched",
                            adminAttendanceService.getTimesheets()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /* ================= LEAVE ================= */

    @PostMapping("/apply-leave/{employeeId}")
    public ResponseEntity<ApiResponse<String>> applyLeave(
            @PathVariable Long employeeId,
            @RequestBody ApplyLeaveRequest request) {
        try {
            adminAttendanceService.applyLeave(
                    employeeId,
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getLeaveType(),
                    request.getReason()
            );
            return ResponseEntity.ok(ApiResponse.success("Leave applied for employee", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/apply-leave-all")
    public ResponseEntity<ApiResponse<String>> applyLeaveAll(
            @RequestBody ApplyLeaveRequest request) {
        try {
            adminAttendanceService.applyLeaveAll(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getLeaveType(),
                    request.getReason()
            );
            return ResponseEntity.ok(ApiResponse.success("Leave applied for all employees", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /* ================= FORCE PUNCH ================= */

    @PostMapping("/force-punch")
    public ResponseEntity<ApiResponse<String>> forcePunch(
            @RequestBody AdminAttendanceRequest request) {
        try {
            if ("punch_in".equalsIgnoreCase(request.getAction())) {
                adminAttendanceService.forcePunchIn(
                        request.getEmployeeId(),
                        request.getTimestamp()
                );
            } else if ("punch_out".equalsIgnoreCase(request.getAction())) {
                adminAttendanceService.forcePunchOut(
                        request.getEmployeeId(),
                        request.getTimestamp()
                );
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid action. Use punch_in or punch_out"));
            }

            return ResponseEntity.ok(ApiResponse.success("Force punch successful", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /* ================= BULK & MANUAL ATTENDANCE ================= */

    @PostMapping("/bulk-action")
    public ResponseEntity<ApiResponse<String>> applyBulkAction(
            @RequestBody BulkActionRequest request) {
        try {
            adminAttendanceService.applyBulkActionForUser(request);
            return ResponseEntity.ok(
                    ApiResponse.success("Bulk attendance applied successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/apply-manual-attendance/{employeeId}")
    public ResponseEntity<ApiResponse<String>> applyManualAttendance(
            @PathVariable Long employeeId,
            @RequestBody ManualAttendanceRequest request) {
        try {
            adminAttendanceService.applyManualAttendance(employeeId, request);
            return ResponseEntity.ok(
                    ApiResponse.success("Manual attendance applied successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/apply-manual-attendance/{employeeId}")
    public ResponseEntity<ApiResponse<String>> updateManualAttendanceAlias(
            @PathVariable Long employeeId,
            @RequestBody ManualAttendanceRequest request) {
        try {
            adminAttendanceService.updateManualAttendance(employeeId, request);
            return ResponseEntity.ok(
                    ApiResponse.success("Manual attendance updated successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }


    /* ================= HR ATTENDANCE (DATE RANGE) ================= */

    @PostMapping("/hr-attendance/{employeeId}")
    public ResponseEntity<ApiResponse<String>> applyHrAttendance(
            @PathVariable Long employeeId,
            @RequestBody HrAttendanceRequest request) {
        try {
            adminAttendanceService.applyBulkActionForUser(
                    employeeId,
                    request.getLeaveType(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getReason()
            );
            return ResponseEntity.ok(
                    ApiResponse.success("HR attendance applied successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /* ================= CALENDAR ================= */

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<?>> getCalendar(
            @RequestParam Long userId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Calendar data fetched",
                            adminAttendanceService.getCalendar(userId, year, month)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /* ================= REQUESTS ================= */

    @GetMapping("/pending-requests")
    public ResponseEntity<ApiResponse<?>> getPendingRequests() {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Pending requests fetched",
                            adminAttendanceService.getPendingRequests()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/approve-request/{id}")
    public ResponseEntity<ApiResponse<String>> approveRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            adminAttendanceService.approveRequest(id, body.get("type"));
            return ResponseEntity.ok(ApiResponse.success("Request approved", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/reject-request/{id}")
    public ResponseEntity<ApiResponse<String>> rejectRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            adminAttendanceService.rejectRequest(
                    id,
                    body.get("type"),
                    body.get("reason")
            );
            return ResponseEntity.ok(ApiResponse.success("Request rejected", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}
