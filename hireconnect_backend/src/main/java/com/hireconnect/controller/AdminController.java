package com.hireconnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:3000"},
	    allowCredentials = "true"
	)
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/employee/{id}")
    public ResponseEntity<ApiResponse<?>> getEmployeeDetail(@PathVariable Long id) {
        try {
            var details = adminService.getEmployeeDetail(id);
            return ResponseEntity.ok(ApiResponse.success("Employee details fetched", details));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/employee/{id}")
    public ResponseEntity<ApiResponse<String>> editEmployee(
            @PathVariable Long id,
            @RequestBody Map<String, Object> employeeData) {
        try {
            adminService.editEmployee(id, employeeData);
            return ResponseEntity.ok(ApiResponse.success("Employee updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/employee/{id}/upload-offer-letter")
    public ResponseEntity<ApiResponse<String>> uploadOfferLetter(
            @PathVariable Long id,
            @RequestParam MultipartFile file) {
        try {
            adminService.uploadOfferLetter(id, file);
            return ResponseEntity.ok(ApiResponse.success("Offer letter uploaded", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/employee/{id}/offer-letter-status")
    public ResponseEntity<ApiResponse<String>> updateOfferLetterStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            adminService.updateOfferLetterStatus(
                    id,
                    request.get("status"),
                    request.get("remarks")
            );
            return ResponseEntity.ok(ApiResponse.success("Offer letter status updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/employee/{id}/generate-id-card")
    public ResponseEntity<ApiResponse<?>> generateIdCard(@PathVariable Long id) {
        try {
            var result = adminService.generateIdCard(id);
            return ResponseEntity.ok(ApiResponse.success("ID card generated", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/employee/{id}/reset-step")
    public ResponseEntity<ApiResponse<String>> resetStep(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            adminService.resetStep(id, request.get("step"));
            return ResponseEntity.ok(ApiResponse.success("Step reset", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/employee/{id}/mark-complete")
    public ResponseEntity<ApiResponse<String>> markComplete(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            adminService.markComplete(id, request.get("complete"));
            return ResponseEntity.ok(ApiResponse.success("Onboarding status updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/employee/{id}/files")
    public ResponseEntity<ApiResponse<?>> getEmployeeFiles(@PathVariable Long id) {
        try {
            var files = adminService.getEmployeeFiles(id);
            return ResponseEntity.ok(ApiResponse.success("Files fetched", files));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/employee/{employeeId}/file/{fileId}")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @PathVariable Long employeeId,
            @PathVariable Long fileId) {
        try {
            adminService.deleteFile(employeeId, fileId);
            return ResponseEntity.ok(ApiResponse.success("File deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/employee/create")
    public ResponseEntity<ApiResponse<?>> createEmployee(
            @RequestParam(required = false) MultipartFile idCard,
            @RequestParam(required = false) MultipartFile offerLetter,
            @RequestParam(required = false) MultipartFile nda,
            @RequestParam Map<String, String> employeeData) {
        try {
            var result = adminService.createEmployee(idCard, offerLetter, nda, employeeData);
            return ResponseEntity.ok(ApiResponse.success("Employee created", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<?>> getAdmins() {
        try {
            var admins = adminService.getAdmins();
            return ResponseEntity.ok(ApiResponse.success("Admins fetched", admins));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody Map<String, String> request) {
        try {
            adminService.changePassword(
                    request.get("currentPassword"),
                    request.get("newPassword"),
                    request.get("confirmPassword")
            );
            return ResponseEntity.ok(ApiResponse.success("Password changed", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(@PathVariable Long id) {
        try {
            adminService.deleteAdmin(id);
            return ResponseEntity.ok(ApiResponse.success("Admin deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
