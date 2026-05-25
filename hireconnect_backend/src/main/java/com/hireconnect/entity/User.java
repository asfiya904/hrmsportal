package com.hireconnect.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------------- Core Identity ---------------- */

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    /* ---------------- Contact ---------------- */

    private String mobile;

    private String phone;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    /* ---------------- Job Details ---------------- */

    // Legacy (kept)
    private String position;

    // Used by frontend
    private String designation;

    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Column(name = "reporting_manager")
    private String reportingManager;

    @Column(name = "work_location")
    private String workLocation;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type")
    private ShiftType shiftType;
    
    

    /* ---------------- Personal ---------------- */

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    /* ---------------- Access & System ---------------- */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "onboarding_step")
    private Integer onboardingStep = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status")
    private OnboardingStatus onboardingStatus = OnboardingStatus.NOT_STARTED;

    @Column(nullable = false)
    private Boolean approved = false;

    /* ---------------- Audit ---------------- */

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(columnDefinition = "TEXT")
    private String profile;

    @Column(name = "offer_letter_url")
    private String offerLetterUrl;

    @Column(name = "offer_letter_uploaded")
    private Boolean offerLetterUploaded = false;

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public EmploymentType getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(EmploymentType employmentType) {
		this.employmentType = employmentType;
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

	public ShiftType getShiftType() {
		return shiftType;
	}

	public void setShiftType(ShiftType shiftType) {
		this.shiftType = shiftType;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Integer getOnboardingStep() {
		return onboardingStep;
	}

	public void setOnboardingStep(Integer onboardingStep) {
		this.onboardingStep = onboardingStep;
	}

	public OnboardingStatus getOnboardingStatus() {
		return onboardingStatus;
	}

	public void setOnboardingStatus(OnboardingStatus onboardingStatus) {
		this.onboardingStatus = onboardingStatus;
	}

	public Boolean getApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}


	public LocalDateTime getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(LocalDateTime lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public Long getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(Long deletedBy) {
		this.deletedBy = deletedBy;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getOfferLetterUrl() {
		return offerLetterUrl;
	}

	public void setOfferLetterUrl(String offerLetterUrl) {
		this.offerLetterUrl = offerLetterUrl;
	}

	public Boolean getOfferLetterUploaded() {
		return offerLetterUploaded;
	}

	public void setOfferLetterUploaded(Boolean offerLetterUploaded) {
		this.offerLetterUploaded = offerLetterUploaded;
	}

	public Boolean getIdCardGenerated() {
		return idCardGenerated;
	}

	public void setIdCardGenerated(Boolean idCardGenerated) {
		this.idCardGenerated = idCardGenerated;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Boolean getTermsAgreed() {
		return termsAgreed;
	}

	public void setTermsAgreed(Boolean termsAgreed) {
		this.termsAgreed = termsAgreed;
	}

	public Boolean getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(Boolean isVerified) {
		this.isVerified = isVerified;
	}

	public String getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public String getResetPasswordToken() {
		return resetPasswordToken;
	}

	public void setResetPasswordToken(String resetPasswordToken) {
		this.resetPasswordToken = resetPasswordToken;
	}

	public LocalDateTime getResetPasswordExpire() {
		return resetPasswordExpire;
	}

	public void setResetPasswordExpire(LocalDateTime resetPasswordExpire) {
		this.resetPasswordExpire = resetPasswordExpire;
	}

	public LocalDateTime getLastStepCompletedAt() {
		return lastStepCompletedAt;
	}

	public void setLastStepCompletedAt(LocalDateTime lastStepCompletedAt) {
		this.lastStepCompletedAt = lastStepCompletedAt;
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

	@Column(name = "id_card_generated")
    private Boolean idCardGenerated = false;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "terms_agreed")
    private Boolean termsAgreed = false;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_expire")
    private LocalDateTime resetPasswordExpire;

    @Column(name = "last_step_completed_at")
    private LocalDateTime lastStepCompletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ---------------- ENUMS ---------------- */

    public enum Role {
        ADMIN, MANAGER, EMPLOYEE, CANDIDATE
    }

    public enum Status {
        ACTIVE, DISABLED, INACTIVE, PENDING
    }

    public enum OnboardingStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETE
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum EmploymentType {
        FULL_TIME, CONTRACT, INTERN
    }

    public enum ShiftType {
        GENERAL, MORNING, EVENING, NIGHT, ROTATIONAL, FLEXIBLE, ON_CALL;

        public static ShiftType from(String value) {
            return ShiftType.valueOf(value.toUpperCase());
        }
    }


    /* ---------------- Lifecycle Hooks ---------------- */

    @PrePersist
    protected void onCreate() {
        if (approved == null) approved = false;
        if (termsAgreed == null) termsAgreed = false;
        if (isVerified == null) isVerified = false;
        if (offerLetterUploaded == null) offerLetterUploaded = false;
        if (idCardGenerated == null) idCardGenerated = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /* ---------------- Getters / Setters (important ones) ---------------- */

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

    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }

    public boolean isEmployee() {
        return Role.EMPLOYEE.equals(role);
    }

    public boolean isCandidate() {
        return Role.CANDIDATE.equals(role);
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(status);
    }
}
