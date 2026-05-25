package com.hireconnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.dto.request.PayrollRequest;
import com.hireconnect.dto.request.PayrollUpdateRequest;
import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.dto.response.PayrollResponse;
import com.hireconnect.entity.PayrollHistory;
import com.hireconnect.entity.User;
import com.hireconnect.repository.UserRepository;
import com.hireconnect.service.PayrollService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:3000"},
	    allowCredentials = "true"
	)
public class PayrollController {
    
    private final PayrollService payrollService;
    private final UserRepository userRepository;
    
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAllPayrolls() {
        try {
            List<PayrollResponse> payrolls = payrollService.getAllPayrolls();
            return ResponseEntity.ok(ApiResponse.success("Payrolls fetched", payrolls));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getMyPayrolls(
            @AuthenticationPrincipal UserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User user = userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PayrollResponse> payrolls =
                payrollService.getPayrollByUser(user.getId());

        return ResponseEntity.ok(
                ApiResponse.success("My payrolls fetched", payrolls)
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(@PathVariable Long id) {
        try {
        	PayrollResponse payroll = payrollService.getPayrollById(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll fetched", payroll));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<PayrollHistory>> createPayroll(@RequestBody PayrollRequest request) {
        try {
            PayrollHistory payroll = payrollService.createPayroll(request);
            return ResponseEntity.ok(ApiResponse.success("Payroll created", payroll));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollHistory>> updatePayroll(
            @PathVariable Long id,
            @RequestBody PayrollUpdateRequest request) {

        PayrollHistory payroll = payrollService.updatePayroll(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payroll updated", payroll));
    }


    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePayroll(@PathVariable Long id) {
        try {
            payrollService.deletePayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate-auto")
    public ResponseEntity<ApiResponse<PayrollHistory>> generateAutoPayroll(@RequestBody PayrollRequest request) {
        try {
            PayrollHistory payroll = payrollService.generateAutoPayroll(request);
            return ResponseEntity.ok(ApiResponse.success("Auto payroll generated", payroll));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/manual-entry")
    public ResponseEntity<ApiResponse<PayrollHistory>> manualPayrollEntry(@RequestBody PayrollRequest request) {
        try {
            PayrollHistory payroll = payrollService.manualPayrollEntry(request);
            return ResponseEntity.ok(ApiResponse.success("Manual payroll created", payroll));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<?>> getAnalytics(@RequestParam(defaultValue = "6months") String range) {
        try {
            var analytics = payrollService.getAnalytics(range);
            return ResponseEntity.ok(ApiResponse.success("Analytics fetched", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<?> exportPayrolls(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String month) {
        try {
            return payrollService.exportPayrolls(format, status, month);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/generate-payslip")
    public ResponseEntity<ApiResponse<?>> generatePayslip(@PathVariable Long id) {
        try {
            var result = payrollService.generatePayslip(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Payslip generated", result)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to generate payslip: " + e.getMessage())
            );
        }
    }
}