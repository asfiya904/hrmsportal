package com.hireconnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.entity.EmployeeProfile;
import com.hireconnect.entity.User;
import com.hireconnect.repository.EmployeeProfileRepository;
import com.hireconnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final PasswordEncoder passwordEncoder;

    /* ================= BASIC CRUD ================= */

    public List<User> getAllEmployees() {
        return userRepository.findByRole(User.Role.EMPLOYEE);
    }

    public Optional<User> getEmployeeById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createEmployee(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.EMPLOYEE);
        user.setStatus(User.Status.PENDING);

        User saved = userRepository.save(user);

        // auto-create empty profile
        EmployeeProfile profile = new EmployeeProfile();
        profile.setUserId(saved.getId());
        employeeProfileRepository.save(profile);

        return saved;
    }

    @Transactional
    public User updateEmployee(Long id, User userDetails) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setMobile(userDetails.getMobile());
        user.setPosition(userDetails.getPosition());
        user.setDepartment(userDetails.getDepartment());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        userRepository.deleteById(id);
    }

    /* ================= STATUS ================= */

    @Transactional
    public void updateEmployeeStatus(Long id, User.Status status) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        user.setStatus(status);
        userRepository.save(user);
    }

    /* ================= PROFILE ================= */

    public Optional<EmployeeProfile> getEmployeeProfile(Long userId) {
        return employeeProfileRepository.findByUserId(userId);
    }

    @Transactional
    public EmployeeProfile updateEmployeeProfile(Long userId, EmployeeProfile profile) {

        EmployeeProfile existing = employeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        
        existing.setDob(profile.getDob());
        existing.setGender(profile.getGender());
        

        return employeeProfileRepository.save(existing);
    }

    /* ================= DASHBOARD ================= */

    public Map<String, Object> getEmployeeDashboard(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("employeeId", user.getId());
        dashboard.put("name", user.getFullName());
        dashboard.put("email", user.getEmail());
        dashboard.put("status", user.getStatus().name());

        return dashboard;
    }

    /* ================= DOCUMENTS ================= */

    public List<?> getEmployeeDocuments(Long userId) {
        // integrate with DocumentService later
        return List.of();
    }

    /* ================= APPROVAL FLOW ================= */

    @Transactional
    public void approveEmployee(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void declineEmployee(Long id, String reason) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        user.setStatus(User.Status.INACTIVE);
        userRepository.save(user);
    }


    /* ================= STATS ================= */

    public Map<String, Object> getEmployeeStats() {

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", userRepository.countByRole(User.Role.EMPLOYEE));
        stats.put("activeEmployees", userRepository.countByStatus(User.Status.ACTIVE));
        stats.put("pendingEmployees", userRepository.countByStatus(User.Status.PENDING));

        return stats;
    }
}
