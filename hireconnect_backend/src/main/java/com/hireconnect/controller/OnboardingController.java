package com.hireconnect.controller;

import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.service.OnboardingService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:3000"},
	    allowCredentials = "true"
	)
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<?>> getOnboardingStatus() {
        try {
            var status = onboardingService.getOnboardingStatus();
            return ResponseEntity.ok(ApiResponse.success("Status fetched", status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/submit-form")
    public ResponseEntity<ApiResponse<String>> submitOnboardingForm(
            @RequestParam(required = false) MultipartFile passportPhoto,
            @RequestParam(required = false) MultipartFile resume,
            @RequestParam(required = false) MultipartFile[] educationCertificates,
            @RequestParam(required = false) MultipartFile[] relievingLetters,
            @RequestParam Map<String, String> formData) {
        try {
            onboardingService.submitOnboardingForm(
                    passportPhoto,
                    resume,
                    educationCertificates,
                    relievingLetters,
                    formData
            );
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Onboarding completed successfully. You will be logged out automatically.",
                            null
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/offer-letter")
    public ResponseEntity<ApiResponse<?>> getOfferLetter() {
        try {
            var offerLetter = onboardingService.getOfferLetter();
            return ResponseEntity.ok(ApiResponse.success("Offer letter fetched", offerLetter));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/upload-offer-letter")
    public ResponseEntity<ApiResponse<String>> uploadOfferLetter(
            @RequestParam MultipartFile file) {
        try {
            onboardingService.uploadOfferLetter(file);
            return ResponseEntity.ok(ApiResponse.success("Offer letter uploaded", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/accept-offer")
    public ResponseEntity<ApiResponse<String>> acceptOffer() {
        try {
            onboardingService.acceptOffer();
            return ResponseEntity.ok(ApiResponse.success("Offer accepted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/generate-id-card")
    public ResponseEntity<ApiResponse<?>> generateIdCard() {
        try {
            var result = onboardingService.generateIdCard();
            return ResponseEntity.ok(ApiResponse.success("ID card generated", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
