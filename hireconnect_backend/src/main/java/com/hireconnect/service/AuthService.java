package com.hireconnect.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.dto.request.LoginRequest;
import com.hireconnect.dto.request.RegisterRequest;
import com.hireconnect.dto.response.AuthResponse;
import com.hireconnect.entity.User;
import com.hireconnect.repository.UserRepository;
import com.hireconnect.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /* ================= REGISTER ================= */

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists");
        }

        User user = new User();

        // Core identity
        user.setEmployeeId(request.getEmployeeId());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

  

        // Contact
        user.setMobile(request.getMobile());
        user.setEmergencyContact(request.getEmergencyContact());

        // Job details
        user.setDepartment(request.getDepartment());
        user.setDesignation(request.getDesignation());
        user.setEmploymentType(request.getEmploymentType());
        user.setReportingManager(request.getReportingManager());
        user.setWorkLocation(request.getWorkLocation());
        user.setJoiningDate(request.getJoiningDate());
        user.setShiftType(request.getShiftType());

        // Personal
        user.setDob(request.getDob());
        user.setGender(request.getGender());

        // Access
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setOnboardingStatus(User.OnboardingStatus.NOT_STARTED);

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                null,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name(),
                savedUser.getOnboardingStatus().name()
        );
    }

    /* ================= LOGIN ================= */

    /* ================= LOGIN ================= */

    public AuthResponse authenticate(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),                 // ADMIN / EMPLOYEE / SUPER_ADMIN
                user.getOnboardingStatus().name()
        );
    }


    /* ================= CURRENT USER ================= */

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}
