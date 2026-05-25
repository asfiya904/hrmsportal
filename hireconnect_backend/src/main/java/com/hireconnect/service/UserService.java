package com.hireconnect.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.dto.request.AdminUpdateEmployeeRequest;
import com.hireconnect.dto.request.SelfProfileUpdateRequest;
import com.hireconnect.entity.User;
import com.hireconnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
   

	public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
    
    @Transactional
    public User updateUser(Long id, AdminUpdateEmployeeRequest req) {
        User user = getUserById(id);

        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }
        if (req.getDesignation() != null) {
            user.setDesignation(req.getDesignation());
        }
        if (req.getDepartment() != null) {
            user.setDepartment(req.getDepartment());
        }
        if (req.getEmploymentType() != null) {
            user.setEmploymentType(
                User.EmploymentType.valueOf(req.getEmploymentType().toUpperCase())
            );
        }
        if (req.getEmploymentType() != null) {
            user.setEmploymentType(
                User.EmploymentType.valueOf(req.getEmploymentType().toUpperCase())
            );
        }
        if (req.getReportingManager() != null) {
            user.setReportingManager(req.getReportingManager());
        }
        if (req.getShiftType() != null) {
            user.setShiftType(
                User.ShiftType.valueOf(req.getShiftType().toUpperCase())
            );
        }
        if (req.getWorkLocation() != null) {
            user.setWorkLocation(req.getWorkLocation());
        }
        if (req.getJoiningDate() != null) {
            user.setJoiningDate(req.getJoiningDate());
        }
        if (req.getDob() != null) {
            user.setDob(req.getDob());
        }
        if (req.getMobile() != null) {
            user.setMobile(req.getMobile());
        }
        if (req.getEmergencyContact() != null) {
            user.setEmergencyContact(req.getEmergencyContact());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }


    //Update Self Profile
    @Transactional
    public User updateCurrentUser(SelfProfileUpdateRequest req) {
        User user = getCurrentUser();

        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }
        if (req.getMobile() != null) {
            user.setMobile(req.getMobile());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User currentUser = getCurrentUser();
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        
        // Update password
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        currentUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(currentUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        
        User userToDelete = getUserById(id);
        User currentUser = getCurrentUser();
        
        // 🔒 Prevent self-delete
        if (userToDelete.getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot delete your own account");
        }
        
        
        userRepository.delete(userToDelete);
    }
    
    public List<User> getUsersByRole(String roleStr) {
        User.Role role = User.Role.valueOf(roleStr.toUpperCase());
        return userRepository.findByRole(role);
    }
    
    public List<User> getUsersByStatus(String statusStr) {
        User.Status status = User.Status.valueOf(statusStr.toUpperCase());
        return userRepository.findByStatus(status);
    }
    
    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
}
    
    

    