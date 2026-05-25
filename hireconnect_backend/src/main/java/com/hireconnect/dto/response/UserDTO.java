package com.hireconnect.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hireconnect.entity.User;

public class UserDTO {

    /* -------- Core Identity -------- */
    private Long id;
    private String employeeId;
    private String fullName;
    private String email;

    /* -------- Contact -------- */
    private String mobile;
    private String phone;
    private String emergencyContact;

    /* -------- Job Details -------- */
    private String department;
    private String designation;
    private String position;
    private String reportingManager;
    private String workLocation;
    private LocalDate joiningDate;
    private String employmentType;
    private String shiftType;

    /* -------- Personal -------- */
    private LocalDate dob;
    private String gender;

    /* -------- Access & System -------- */
    private String role;
    private String status;
    private Integer onboardingStep;
    private String onboardingStatus;
    private Boolean approved;
    private Boolean isAdmin;
    private String adminRole;

    /* -------- Audit -------- */
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // No-args constructor
    public UserDTO() {
    }

    // Constructor from User entity
    public UserDTO(User user) {

        this.id = user.getId();
        this.employeeId = user.getEmployeeId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();

        this.mobile = user.getMobile();
        this.phone = user.getPhone();
        this.emergencyContact = user.getEmergencyContact();

        this.department = user.getDepartment();
        this.designation = user.getDesignation();
        this.position = user.getPosition();
        this.reportingManager = user.getReportingManager();
        this.workLocation = user.getWorkLocation();
        this.joiningDate = user.getJoiningDate();

        this.shiftType = user.getShiftType() != null ? user.getShiftType().name() : null;
        this.employmentType = user.getEmploymentType() != null ? user.getEmploymentType().name() : null;

        this.dob = user.getDob();
        this.gender = user.getGender() != null ? user.getGender().name() : null;

        this.role = user.getRole() != null ? user.getRole().name() : null;
        this.status = user.getStatus() != null ? user.getStatus().name() : null;

        this.onboardingStep = user.getOnboardingStep();
        this.onboardingStatus = user.getOnboardingStatus() != null
                ? user.getOnboardingStatus().name()
                : null;

        this.approved = user.getApproved();
        this.isAdmin = user.isAdmin();

        this.lastLoginAt = user.getLastLoginAt();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    /* -------- Getters & Setters -------- */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getShiftType() {
        return shiftType;
    }

    public void setShiftType(String shiftType) {
        this.shiftType = shiftType;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getOnboardingStep() {
        return onboardingStep;
    }

    public void setOnboardingStep(Integer onboardingStep) {
        this.onboardingStep = onboardingStep;
    }

    public String getOnboardingStatus() {
        return onboardingStatus;
    }

    public void setOnboardingStatus(String onboardingStatus) {
        this.onboardingStatus = onboardingStatus;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
