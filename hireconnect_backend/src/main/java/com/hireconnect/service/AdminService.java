// AdminService.java

package com.hireconnect.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hireconnect.entity.AuditLog;
import com.hireconnect.entity.Document;
import com.hireconnect.entity.User;
import com.hireconnect.repository.AuditLogRepository;
import com.hireconnect.repository.DocumentRepository;
import com.hireconnect.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    private static final String UPLOAD_DIR = "uploads/";

    /* ================= EMPLOYEE DETAILS ================= */

    public Map<String, Object> getEmployeeDetail(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("employee", employee);
        result.put("documents", documentRepository.findByUserId(employeeId));
        result.put("auditTrail",
                auditLogRepository.findByUserIdOrderByCreatedAtDesc(employeeId));

        return result;
    }

    /* ================= EDIT EMPLOYEE ================= */

    @Transactional
    public void editEmployee(Long employeeId, Map<String, Object> data) {
        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (data.containsKey("fullName")) {
            user.setFullName((String) data.get("fullName"));
        }

        if (data.containsKey("email")) user.setEmail((String) data.get("email"));
        if (data.containsKey("phone")) user.setMobile((String) data.get("phone"));
        if (data.containsKey("department")) user.setDepartment((String) data.get("department"));
        if (data.containsKey("designation")) user.setDesignation((String) data.get("designation"));

        if (data.containsKey("role")) {
            user.setRole(User.Role.valueOf(data.get("role").toString().toUpperCase()));
        }
        if (data.containsKey("status")) {
            user.setStatus(User.Status.valueOf(data.get("status").toString().toUpperCase()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        createAuditLog(employeeId, "EDIT_EMPLOYEE", "Employee updated");
    }

    /* ================= OFFER LETTER ================= */

    @Transactional
    public void uploadOfferLetter(Long employeeId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new RuntimeException("File required");
        if (!"application/pdf".equals(file.getContentType()))
            throw new RuntimeException("Only PDF allowed");

        String path = saveFile(file, "offer-letters");

        Document doc = new Document();
        doc.setUserId(employeeId);
        doc.setDocumentType(Document.DocumentType.OFFER_LETTER);

        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(path);
        doc.setFileSize(file.getSize());
        doc.setStatus(Document.DocumentStatus.PENDING);
        documentRepository.save(doc);

        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        user.setOfferLetterUrl(path);
        userRepository.save(user);

        createAuditLog(employeeId, "UPLOAD_OFFER_LETTER", "Offer letter uploaded");
    }

    @Transactional
    public void updateOfferLetterStatus(Long employeeId, String status, String remarks) {
    	Document doc = documentRepository
    	        .findLatestByUserIdAndDocumentType(
    	        	    employeeId,
    	        	    Document.DocumentType.OFFER_LETTER
    	        	)
    	        .orElseThrow(() -> new RuntimeException("Offer letter not found"));


        doc.setStatus(Document.DocumentStatus.valueOf(status.toUpperCase()));
        doc.setRemarks(remarks);
        documentRepository.save(doc);

        createAuditLog(employeeId, "OFFER_LETTER_STATUS",
                "Status: " + status + ", Remarks: " + remarks);
    }

    /* ================= ID CARD ================= */

    @Transactional
    public Map<String, Object> generateIdCard(Long employeeId) {
        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        user.setIdCardGenerated(true);

        userRepository.save(user);

        createAuditLog(employeeId, "GENERATE_ID_CARD", "ID card generated");

        Map<String, Object> result = new HashMap<>();
        result.put("idCardNumber", user.getEmployeeId());
        return result;

    }

    /* ================= ONBOARDING ================= */

    @Transactional
    public void resetStep(Long employeeId, Integer step) {
        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        user.setOnboardingStep(step);
        userRepository.save(user);

        createAuditLog(employeeId, "RESET_STEP", "Reset to step " + step);
    }

    @Transactional
    public void markComplete(Long employeeId, Boolean complete) {
        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        user.setOnboardingStatus(
                complete ? User.OnboardingStatus.COMPLETE
                         : User.OnboardingStatus.IN_PROGRESS
        );

        userRepository.save(user);

        createAuditLog(employeeId, "ONBOARDING_COMPLETE",
                "Completed: " + complete);
    }

    /* ================= FILES ================= */

    public List<Document> getEmployeeFiles(Long employeeId) {
        return documentRepository.findByUserId(employeeId);
    }

    @Transactional
    public void deleteFile(Long employeeId, Long fileId) {
        Document doc = documentRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!doc.getUserId().equals(employeeId))
            throw new RuntimeException("Unauthorized");

        documentRepository.delete(doc);
        createAuditLog(employeeId, "DELETE_FILE", "File deleted");
    }

    /* ================= CREATE EMPLOYEE ================= */

    @Transactional
    public User createEmployee(
            MultipartFile idCard,
            MultipartFile offerLetter,
            MultipartFile nda,
            Map<String, String> data) {

        User user = new User();

        // ---------- Core identity ----------
        user.setEmployeeId(data.get("employeeId"));
        if (userRepository.existsByEmployeeId(user.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists");
        }
        user.setFullName(data.get("fullName"));
        user.setEmail(data.get("email"));
        user.setPassword(passwordEncoder.encode(data.get("password")));

        // ---------- Job & organization ----------
        user.setDepartment(data.get("department"));
        user.setDesignation(data.get("designation"));
        if (data.get("employmentType") != null) {
            user.setEmploymentType(
                User.EmploymentType.valueOf(
                    data.get("employmentType").toUpperCase()
                )
            );
        }
        user.setReportingManager(data.get("reportingManager"));
        user.setWorkLocation(data.get("workLocation"));
        if (data.get("shiftType") != null) {
            user.setShiftType(
                User.ShiftType.valueOf(
                    data.get("shiftType").toUpperCase()
                )
            );
        }


        // ---------- Dates ----------
        if (data.get("joiningDate") != null)
            user.setJoiningDate(LocalDate.parse(data.get("joiningDate")));

        if (data.get("dob") != null)
            user.setDob(LocalDate.parse(data.get("dob")));

        // ---------- Personal ----------
        user.setMobile(data.get("mobile"));
        if (data.get("gender") != null) {
            user.setGender(
                User.Gender.valueOf(
                    data.get("gender").toUpperCase()
                )
            );
        }
        user.setEmergencyContact(data.get("emergencyContact"));

        // ---------- Role & status ----------
        user.setRole(
            User.Role.valueOf(
                data.getOrDefault("role", "EMPLOYEE").toUpperCase()
            )
        );
        user.setStatus(
            User.Status.valueOf(
                data.getOrDefault("status", "ACTIVE").toUpperCase()
            )
        );

        // ✅ SAVE AFTER ALL FIELDS ARE SET
        userRepository.save(user);

        if (offerLetter != null)
            uploadOfferLetter(user.getId(), offerLetter);

        createAuditLog(user.getId(), "CREATE_EMPLOYEE", "Employee created");
        return user;
    }


    /* ================= ADMINS ================= */

    public List<User> getAdmins() {
        return userRepository.findByRole(User.Role.ADMIN);
    }

    @Transactional
    public void deleteAdmin(Long adminId) {
        userRepository.deleteById(adminId);
    }

    /* ================= PASSWORD ================= */

    @Transactional
    public void changePassword(String current, String newPass, String confirm) {
        User user = userService.getCurrentUser();

        if (!passwordEncoder.matches(current, user.getPassword()))
            throw new RuntimeException("Invalid current password");
        if (!newPass.equals(confirm))
            throw new RuntimeException("Passwords do not match");

        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);
    }

    /* ================= HELPERS ================= */

    private String saveFile(MultipartFile file, String subDir) {
        try {
            String dir = UPLOAD_DIR + subDir + "/";
            File folder = new File(dir);
            if (!folder.exists()) folder.mkdirs();

            String name = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(dir + name);
            Files.write(path, file.getBytes());

            return "/" + dir + name;
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    private void createAuditLog(Long userId, String action, String details) {
        User admin = userService.getCurrentUser();

        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setPerformedBy(admin.getId());
        log.setAction(action);
        log.setDetails(details);

        auditLogRepository.save(log);
    }
}
