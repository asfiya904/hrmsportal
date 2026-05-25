package com.hireconnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.entity.EmployeeProfile;
import com.hireconnect.entity.User;
import com.hireconnect.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:3000"},
	    allowCredentials = "true"
	)
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllEmployees() {
        try {
            List<User> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(ApiResponse.success("Employees fetched", employees));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getEmployeeStats() {
        try {
            var stats = employeeService.getEmployeeStats();
            return ResponseEntity.ok(ApiResponse.success("Stats fetched", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getEmployeeById(@PathVariable Long id) {
        try {
            User employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            return ResponseEntity.ok(ApiResponse.success("Employee fetched", employee));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createEmployee(@RequestBody User user) {
        try {
            User created = employeeService.createEmployee(user);
            return ResponseEntity.ok(ApiResponse.success("Employee created", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateEmployee(
            @PathVariable Long id, 
            @RequestBody User user) {
        try {
            User updated = employeeService.updateEmployee(id, user);
            return ResponseEntity.ok(ApiResponse.success("Employee updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateEmployeeStatus(
            @PathVariable Long id,
            @RequestBody User user) {
        try {
            employeeService.updateEmployeeStatus(id, user.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Status updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(ApiResponse.success("Employee deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<EmployeeProfile>> getEmployeeProfile(@PathVariable Long id) {
        try {
            EmployeeProfile profile = employeeService.getEmployeeProfile(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
            return ResponseEntity.ok(ApiResponse.success("Profile fetched", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<EmployeeProfile>> updateEmployeeProfile(
            @PathVariable Long id,
            @RequestBody EmployeeProfile profile) {
        try {
            EmployeeProfile updated = employeeService.updateEmployeeProfile(id, profile);
            return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<ApiResponse<?>> getEmployeeDashboard(@PathVariable Long id) {
        try {
            var dashboard = employeeService.getEmployeeDashboard(id);
            return ResponseEntity.ok(ApiResponse.success("Dashboard fetched", dashboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<?>> getEmployeeDocuments(@PathVariable Long id) {
        try {
            var documents = employeeService.getEmployeeDocuments(id);
            return ResponseEntity.ok(ApiResponse.success("Documents fetched", documents));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveEmployee(@PathVariable Long id) {
        try {
            employeeService.approveEmployee(id);
            return ResponseEntity.ok(ApiResponse.success("Employee approved", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/decline")
    public ResponseEntity<ApiResponse<String>> declineEmployee(
            @PathVariable Long id,
            @RequestBody(required = false) String reason) {
        try {
            employeeService.declineEmployee(id, reason);
            return ResponseEntity.ok(ApiResponse.success("Employee declined", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}