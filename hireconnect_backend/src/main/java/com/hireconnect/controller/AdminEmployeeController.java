package com.hireconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.dto.request.AdminCreateEmployeeRequest;
import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.entity.User;
import com.hireconnect.service.AdminEmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService service;

    // ✅ Create employee
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createEmployee(
            @RequestBody AdminCreateEmployeeRequest request) {

    	User createdUser = service.createEmployee(request);

    	return ResponseEntity.ok(
    	    ApiResponse.success("Employee created successfully", createdUser)
    	);
    }

    // ✅ Get employee by ID (required for attendance & timesheet mapping)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getEmployeeById(
            @PathVariable Long id) {

        User user = service.getEmployeeById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Employee fetched successfully", user)
        );

    }
}
