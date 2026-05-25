package com.hireconnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.hireconnect.dto.request.AdminUpdateEmployeeRequest;
import com.hireconnect.dto.request.SelfProfileUpdateRequest;
import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.dto.response.AuthResponse;
import com.hireconnect.entity.User;
import com.hireconnect.service.AuthService;
import com.hireconnect.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:3000"},
	    allowCredentials = "true"
	)
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /* ================= EMPLOYEE MANAGEMENT ================= */

    // Get all employees
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(
            ApiResponse.success("Employees fetched successfully", users)
        );
    }

    // Get employee by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Employee fetched successfully", user)
        );
    }

    // Update employee (Admin action)
    @PutMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateEmployee(
            @PathVariable Long id,
            @RequestBody AdminUpdateEmployeeRequest request) {

        User updated = userService.updateUser(id, request);

        return ResponseEntity.ok(
            ApiResponse.success("Employee updated successfully", updated)
        );
    }


    // Delete employee (Admin action)
    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
            ApiResponse.success("Employee deleted successfully", null)
        );
    }

    /* ================= CURRENT USER ================= */

    // Get logged-in user
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser() {

        User user = authService.getCurrentUser();

        AuthResponse response = new AuthResponse(
            null, // token not needed
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getOnboardingStatus().name()
        );

        return ResponseEntity.ok(
            ApiResponse.success("Current user fetched", response)
        );
    }

    // Update logged-in employee's own  (self profile)
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<User>> updateCurrentUser(
            @RequestBody SelfProfileUpdateRequest request) {

        User updated = userService.updateCurrentUser(request);

        return ResponseEntity.ok(
            ApiResponse.success("Profile updated successfully", updated)
        );
    }


    // Change password
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody Map<String, String> request) {

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        userService.changePassword(oldPassword, newPassword);

        return ResponseEntity.ok(
            ApiResponse.success("Password changed successfully", null)
        );
    }

    /* ================= ADMIN UTILITIES ================= */

    // Get admins
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAdmins() {

        List<User> admins = userService.getUsersByRole("ADMIN");

        return ResponseEntity.ok(
            ApiResponse.success("Admins fetched successfully", admins)
        );
    }
    
    @PutMapping("/admins/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateAdmin(
            @PathVariable Long id,
            @RequestBody AdminUpdateEmployeeRequest request) {

        User updated = userService.updateUser(id, request);

        return ResponseEntity.ok(
            ApiResponse.success("Admin updated successfully", updated)
        );
    }
    
    @DeleteMapping("/admins/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
            ApiResponse.success("Admin deleted successfully", null)
        );
    }



    // Filter users by role
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(
            @PathVariable String role) {

        List<User> users = userService.getUsersByRole(role);

        return ResponseEntity.ok(
            ApiResponse.success("Users fetched successfully", users)
        );
    }

    // Filter users by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByStatus(
            @PathVariable String status) {

        List<User> users = userService.getUsersByStatus(status);

        return ResponseEntity.ok(
            ApiResponse.success("Users fetched successfully", users)
        );
    }

    // Search users
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(
            @RequestParam String query) {

        List<User> users = userService.searchUsers(query);

        return ResponseEntity.ok(
            ApiResponse.success("Search results fetched", users)
        );
    }
}
