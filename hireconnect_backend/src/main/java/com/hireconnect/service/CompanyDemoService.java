package com.hireconnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.dto.request.CompanyDemoLoginRequest;
import com.hireconnect.dto.request.CompanyDemoRequest;
import com.hireconnect.dto.request.CompanyDemoUpdateRequest;
import com.hireconnect.dto.request.CompanyLoginRequest;
import com.hireconnect.dto.request.CompanyRegistrationRequest;
import com.hireconnect.dto.response.CompanyDemoResponse;
import com.hireconnect.dto.response.CompanyRegistrationResponse;
import com.hireconnect.entity.CompanyDemoDetails;
import com.hireconnect.entity.CompanyDemoDetails.DemoStatus;
import com.hireconnect.entity.CompanyRegistration;
import com.hireconnect.repository.CompanyDemoRepository;
import com.hireconnect.repository.CompanyRegistrationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyDemoService {

    private final CompanyDemoRepository companyDemoRepository;
    private final CompanyRegistrationRepository companyRegistrationRepository;
    private final PasswordEncoder passwordEncoder;

    /* ================= DEMO REGISTRATION ================= */

    @Transactional
    public CompanyDemoResponse register(CompanyDemoRequest request) {

        if (companyDemoRepository.existsByCompanyEmail(request.getCompanyEmail())) {
            throw new RuntimeException("Company email already registered");
        }

        CompanyDemoDetails company = new CompanyDemoDetails();
        company.setFullName(request.getFullName());
        company.setCompanyEmail(request.getCompanyEmail());
        company.setPhoneNumber(request.getPhoneNumber());
        company.setCompanyName(request.getCompanyName());
        company.setDesignation(request.getDesignation());
        company.setStatus(DemoStatus.PENDING);

        return toDemoResponse(companyDemoRepository.save(company));
    }

    /* ================= DEMO LOGIN ================= */

    public CompanyDemoResponse login(CompanyDemoLoginRequest request) {

        CompanyDemoDetails company = companyDemoRepository
                .findByCompanyEmail(request.getCompanyEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        return toDemoResponse(company);
    }


    
    @Transactional
    public CompanyRegistrationResponse registerCompany(CompanyRegistrationRequest request){

        if (companyRegistrationRepository.existsByCompanyEmail(request.getCompanyEmail())) {
            throw new RuntimeException("Company email already registered");
        }

        CompanyRegistration company = new CompanyRegistration();
        company.setCompanyEmail(request.getCompanyEmail());
        company.setCompanyName(request.getCompanyName());
        company.setCompanyKey(request.getCompanyKey());
        company.setPassword(passwordEncoder.encode(request.getPassword())); // NOTE: Password stored for Phase 2, not used in Phase 1 login



        if (request.getRole() != null && !request.getRole().isBlank()) {
            company.setRole(
                CompanyRegistration.CompanyRole.valueOf(
                    request.getRole().toUpperCase()
                )
            );
        } else {
            company.setRole(CompanyRegistration.CompanyRole.ADMIN);
        }


        return toCompanyResponse(companyRegistrationRepository.save(company));
    }

    /* ================= COMPANY LOGIN ================= */

    /* ================= COMPANY LOGIN ================= */

    public CompanyRegistrationResponse loginRegisteredCompany(
            CompanyLoginRequest request) {

        CompanyRegistration company =
                companyRegistrationRepository.findByCompanyEmail(request.getCompanyEmail());

        if (company == null || !company.getCompanyKey().equals(request.getCompanyKey())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Phase 1: UI-only company login, no JWT here
        return toCompanyResponse(company);
    }


    /* ================= READ ================= */

    public List<CompanyDemoResponse> getAllCompanies() {
        return companyDemoRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::toDemoResponse)
                .collect(Collectors.toList());
    }

    public List<CompanyRegistrationResponse> getAllRegisteredCompanies() {
        return companyRegistrationRepository.findAll()
                .stream()
                .map(this::toCompanyResponse)
                .collect(Collectors.toList());
    }

    public CompanyDemoResponse getCompanyById(Long id) {
        CompanyDemoDetails company = companyDemoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return toDemoResponse(company);
    }

    public List<CompanyDemoResponse> getCompaniesByStatus(String status) {
        DemoStatus demoStatus = DemoStatus.valueOf(status.toUpperCase());

        return companyDemoRepository.findByStatus(demoStatus)
                .stream()
                .map(this::toDemoResponse)
                .collect(Collectors.toList());
    }

    public List<CompanyDemoResponse> searchCompaniesByName(String companyName) {
        return companyDemoRepository
                .findByCompanyNameContainingIgnoreCase(companyName)
                .stream()
                .map(this::toDemoResponse)
                .collect(Collectors.toList());
    }

    /* ================= UPDATE ================= */

    @Transactional
    public CompanyDemoResponse updateCompany(Long id, CompanyDemoUpdateRequest request) {

        CompanyDemoDetails company = companyDemoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (request.getFullName() != null) company.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) company.setPhoneNumber(request.getPhoneNumber());
        if (request.getCompanyName() != null) company.setCompanyName(request.getCompanyName());
        if (request.getDesignation() != null) company.setDesignation(request.getDesignation());
        if (request.getRemarks() != null) company.setRemarks(request.getRemarks());

        if (request.getStatus() != null) {
            company.setStatus(DemoStatus.valueOf(request.getStatus().toUpperCase()));
        }

        return toDemoResponse(companyDemoRepository.save(company));
    }

    @Transactional
    public CompanyDemoResponse updateStatus(Long id, String status, String remarks) {

        CompanyDemoDetails company = companyDemoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        company.setStatus(DemoStatus.valueOf(status.toUpperCase()));
        company.setRemarks(remarks);

        return toDemoResponse(companyDemoRepository.save(company));
    }

    /* ================= DELETE ================= */

    @Transactional
    public void deleteCompany(Long id) {
        if (!companyDemoRepository.existsById(id)) {
            throw new RuntimeException("Company not found");
        }
        companyDemoRepository.deleteById(id);
    }

    /* ================= DASHBOARD ================= */

    public Map<String, Object> getStatistics() {

        Map<String, Object> stats = new HashMap<>();

        for (DemoStatus status : DemoStatus.values()) {
            stats.put(
                    status.name().toLowerCase(),
                    companyDemoRepository.countByStatus(status)
            );
        }

        stats.put("totalCompanies", companyDemoRepository.count());
        return stats;
    }

    /* ================= MAPPERS ================= */

    private CompanyDemoResponse toDemoResponse(CompanyDemoDetails company) {
        CompanyDemoResponse r = new CompanyDemoResponse();
        r.setId(company.getId());
        r.setFullName(company.getFullName());
        r.setCompanyEmail(company.getCompanyEmail());
        r.setPhoneNumber(company.getPhoneNumber());
        r.setCompanyName(company.getCompanyName());
        r.setDesignation(company.getDesignation());
        r.setStatus(company.getStatus().name());
        r.setCreatedAt(company.getCreatedAt());
        r.setUpdatedAt(company.getUpdatedAt());
        r.setRemarks(company.getRemarks());
        return r;
    }

    private CompanyRegistrationResponse toCompanyResponse(CompanyRegistration company) {
        CompanyRegistrationResponse r = new CompanyRegistrationResponse();
        r.setId(company.getId());
        r.setCompanyEmail(company.getCompanyEmail());
        r.setCompanyName(company.getCompanyName());
        r.setCompanyKey(company.getCompanyKey());
        r.setRole(
        	    company.getRole() != null 
        	        ? company.getRole().name() 
        	        : "ADMIN"
        	);

        return r;
    }
}
