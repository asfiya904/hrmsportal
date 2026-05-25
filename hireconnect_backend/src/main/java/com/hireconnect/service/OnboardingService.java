package com.hireconnect.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.entity.Document;
import com.hireconnect.entity.EmployeeProfile;
import com.hireconnect.entity.OnboardingAnswer;
import com.hireconnect.entity.User;
import com.hireconnect.repository.DocumentRepository;
import com.hireconnect.repository.EmployeeProfileRepository;
import com.hireconnect.repository.OnboardingAnswerRepository;
import com.hireconnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final OnboardingAnswerRepository onboardingAnswerRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final DocumentRepository documentRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    private static final String UPLOAD_DIR = "uploads/";

    /* ================= STATUS ================= */

    public Map<String, Object> getOnboardingStatus() {
        User currentUser = userService.getCurrentUser();

        Map<String, Object> status = new HashMap<>();
        status.put("step", currentUser.getOnboardingStep());
        status.put("status", currentUser.getOnboardingStatus().name());
        return status;
    }

    /* ================= SUBMIT FORM ================= */

    @Transactional
    public void submitOnboardingForm(
            MultipartFile passportPhoto,
            MultipartFile resume,
            MultipartFile[] educationCertificates,
            MultipartFile[] relievingLetters,
            Map<String, String> formData
    ) throws IOException {

        User currentUser = userService.getCurrentUser();

        // Save form data
        OnboardingAnswer answer = new OnboardingAnswer();
        answer.setUserId(currentUser.getId());
        answer.setStep(1);
        answer.setData(objectMapper.writeValueAsString(formData));
        answer.setSubmittedAt(LocalDateTime.now());
        onboardingAnswerRepository.save(answer);

        // Passport photo
        String photoPath = saveFile(passportPhoto, "passport-photos");
        if (photoPath != null) {
            saveDocument(
                currentUser.getId(),
                Document.DocumentType.PASSPORT_PHOTO,
                passportPhoto.getOriginalFilename(),
                photoPath
            );
        }

        // Resume
        if (resume != null && !resume.isEmpty()) {
            String resumePath = saveFile(resume, "resumes");
            saveDocument(
                currentUser.getId(),
                Document.DocumentType.EXPERIENCE_LETTER,
                resume.getOriginalFilename(),
                resumePath
            );
        }

        // Education certificates
        if (educationCertificates != null) {
            for (MultipartFile cert : educationCertificates) {
                String certPath = saveFile(cert, "education-certificates");
                saveDocument(
                    currentUser.getId(),
                    Document.DocumentType.DEGREE_CERTIFICATE,
                    cert.getOriginalFilename(),
                    certPath
                );
            }
        }

        // Relieving letters
        if (relievingLetters != null) {
            for (MultipartFile letter : relievingLetters) {
                String letterPath = saveFile(letter, "relieving-letters");
                saveDocument(
                    currentUser.getId(),
                    Document.DocumentType.EXPERIENCE_LETTER,
                    letter.getOriginalFilename(),
                    letterPath
                );
            }
        }

        // Update profile
        updateEmployeeProfile(currentUser.getId(), formData, photoPath);

        // Update onboarding status
        currentUser.setOnboardingStatus(User.OnboardingStatus.COMPLETE);
        currentUser.setOnboardingStep(1);
        userRepository.save(currentUser);
    }

    /* ================= OFFER LETTER ================= */

    public Map<String, Object> getOfferLetter() {
        User currentUser = userService.getCurrentUser();

        Document offerLetter = documentRepository
            .findLatestByUserIdAndDocumentType(
                currentUser.getId(),
                Document.DocumentType.OFFER_LETTER
            )
            .orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (offerLetter != null) {
            result.put("id", offerLetter.getId());
            result.put("filePath", offerLetter.getFilePath());
            result.put("status", offerLetter.getStatus().name());
            result.put("uploadedAt", offerLetter.getUploadedAt());
        }
        return result;
    }

    @Transactional
    public void uploadOfferLetter(MultipartFile file) throws IOException {
        User currentUser = userService.getCurrentUser();

        String filePath = saveFile(file, "offer-letters");
        saveDocument(
            currentUser.getId(),
            Document.DocumentType.OFFER_LETTER,
            file.getOriginalFilename(),
            filePath
        );

        currentUser.setOfferLetterUploaded(true);
        userRepository.save(currentUser);
    }

    @Transactional
    public void acceptOffer() {
        User currentUser = userService.getCurrentUser();
        currentUser.setOfferLetterUploaded(true);
        currentUser.setOnboardingStep(4);
        userRepository.save(currentUser);
    }

    /* ================= ID CARD ================= */

    @Transactional
    public Map<String, Object> generateIdCard() {
        User currentUser = userService.getCurrentUser();

        String cardNumber = "HRC" + System.currentTimeMillis() + currentUser.getId();
        String filePath = "/uploads/id-cards/" + cardNumber + ".pdf";

        // ID card is NOT a Document
        currentUser.setIdCardGenerated(true);
        currentUser.setOnboardingStatus(User.OnboardingStatus.COMPLETE);
        userRepository.save(currentUser);

        Map<String, Object> result = new HashMap<>();
        result.put("cardNumber", cardNumber);
        result.put("filePath", filePath);
        result.put("message", "ID card generated successfully");
        return result;
    }

    /* ================= HELPERS ================= */

    private String saveFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String directory = UPLOAD_DIR + subDirectory + "/";
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(directory + filename);
        Files.write(path, file.getBytes());

        return "/" + directory + filename;
    }

    private void saveDocument(
            Long userId,
            Document.DocumentType documentType,
            String fileName,
            String filePath
    ) {
        Document document = new Document();
        document.setUserId(userId);
        document.setDocumentType(documentType);
        document.setFileName(fileName);
        document.setFilePath(filePath);
        document.setStatus(Document.DocumentStatus.APPROVED);
        documentRepository.save(document);
    }

    private void updateEmployeeProfile(
            Long userId,
            Map<String, String> formData,
            String photoPath
    ) {
        EmployeeProfile profile = employeeProfileRepository
            .findByUserId(userId)
            .orElse(new EmployeeProfile());

        profile.setUserId(userId);
        profile.setFullName(formData.get("fullName"));
        profile.setEmail(formData.get("personalEmail"));
        profile.setMobile(formData.get("mobileNumber"));
        profile.setDob(formData.get("dob") != null ? LocalDate.parse(formData.get("dob")) : null);
        profile.setGender(formData.get("gender"));
        profile.setNationality(formData.get("nationality"));
        profile.setPosition(formData.get("jobTitle"));
        profile.setExperience(formData.get("experience"));
        profile.setLocation(formData.get("workLocation"));
        profile.setPhoto(photoPath);

        employeeProfileRepository.save(profile);
    }
}
