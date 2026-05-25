package com.hireconnect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.dto.request.AdminCreateEmployeeRequest;
import com.hireconnect.entity.User;
import com.hireconnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ================= CREATE EMPLOYEE =================
    @Transactional
    public User createEmployee(AdminCreateEmployeeRequest r) {

        // ---- Uniqueness checks ----
        if (userRepository.existsByEmail(r.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByEmployeeId(r.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists");
        }

        // ---- Password validation (admin sets password manually) ----
        if (r.getPassword() == null || r.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        // ---- Mandatory enum validations ----
        if (r.getEmploymentType() == null) {
            throw new RuntimeException("Employment type is required");
        }

        if (r.getShiftType() == null) {
            throw new RuntimeException("Shift type is required");
        }

        if (r.getGender() == null) {
            throw new RuntimeException("Gender is required");
        }

        // ---- Create User ----
        User user = new User();

        // Core identity
        user.setEmployeeId(r.getEmployeeId());
        user.setFullName(r.getFullName());
        user.setEmail(r.getEmail());
        user.setPassword(passwordEncoder.encode(r.getPassword()));

        // Contact
        user.setMobile(r.getMobile());
        user.setEmergencyContact(r.getEmergencyContact());

        // Job details
        user.setDepartment(r.getDepartment());
        user.setDesignation(r.getDesignation());
        user.setEmploymentType(r.getEmploymentType());
        user.setReportingManager(r.getReportingManager());
        user.setWorkLocation(r.getWorkLocation());
        user.setJoiningDate(r.getJoiningDate());
        user.setShiftType(r.getShiftType());

        // Personal
        user.setDob(r.getDob());
        user.setGender(r.getGender());

        // Access & system
        user.setRole(
            r.getRole() != null ? r.getRole() : User.Role.EMPLOYEE
        );

        user.setStatus(
            r.getStatus() != null ? r.getStatus() : User.Status.ACTIVE
        );

        // Admin-created employees are auto-approved
        user.setApproved(true);

        // ---- Persist & return ----
        return userRepository.save(user);
    }

    // ================= FETCH EMPLOYEE BY ID =================
    public User getEmployeeById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }
}
