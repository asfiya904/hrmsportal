package com.hireconnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(
	    origins = "http://localhost:3000",
	    allowCredentials = "true"
	)
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/employee/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeDashboard(
            @PathVariable Long id) {

        Map<String, Object> dashboard = dashboardService.getEmployeeDashboard(id);
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data fetched", dashboard)
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminDashboard() {
        Map<String, Object> dashboard = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(
                ApiResponse.success("Admin dashboard fetched", dashboard)
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(
                ApiResponse.success("Stats fetched", stats)
        );
    }

    @GetMapping("/upcoming-events")
    public ResponseEntity<ApiResponse<?>> getUpcomingEvents(
            @RequestParam(defaultValue = "7") int days) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Upcoming events fetched",
                        dashboardService.getUpcomingEvents(days)
                )
        );
    }
}
