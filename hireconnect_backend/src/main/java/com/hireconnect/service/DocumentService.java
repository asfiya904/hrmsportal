package com.hireconnect.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hireconnect.entity.Document;
import com.hireconnect.entity.User;
import com.hireconnect.repository.DocumentRepository;
import com.hireconnect.repository.UserRepository;

@Service
public class DocumentService {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    private static final String UPLOAD_DIR = "uploads/documents/";
    
    
    private static final Map<String, Document.DocumentType> DOCUMENT_TYPE_MAPPING = Map.ofEntries(
        Map.entry("tenthMarksheet", Document.DocumentType.TENTH_MARKSHEET),
        Map.entry("twelfthMarksheet", Document.DocumentType.TWELFTH_MARKSHEET),
        Map.entry("graduationMarksheet", Document.DocumentType.GRADUATION_MARKSHEET),
        Map.entry("postGraduationMarksheet", Document.DocumentType.POST_GRADUATION_MARKSHEET),
        Map.entry("degreeCertificate", Document.DocumentType.DEGREE_CERTIFICATE),
        Map.entry("aadharCard", Document.DocumentType.AADHAR_CARD),
        Map.entry("panCard", Document.DocumentType.PAN_CARD),
        Map.entry("passportPhoto", Document.DocumentType.PASSPORT_PHOTO),
        Map.entry("offerLetter", Document.DocumentType.OFFER_LETTER),
        Map.entry("experienceLetter", Document.DocumentType.EXPERIENCE_LETTER)
    );
    
    public List<Document> getCurrentUserDocuments() {
        Long currentUserId = userService.getCurrentUser().getId();
        return documentRepository.findByUserIdOrderByUploadedAtDesc(currentUserId);
    }
    
    public List<Document> getAllDocuments() {
    	User currentUser = userService.getCurrentUser();

    	if (!currentUser.isAdmin()) {
    	    throw new RuntimeException("Access denied");
    	}
        return documentRepository.findAllByOrderByUploadedAtDesc();
    }
    
    public List<Document> getUserDocuments(Long userId) {
        userService.getUserById(userId);
        return documentRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }
    
    public Document getDocumentById(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        
        User currentUser = userService.getCurrentUser();
        if (!document.getUserId().equals(currentUser.getId()) &&
        	    !currentUser.isAdmin()) {
        	    throw new RuntimeException("Access denied");
        	}
        
        return document;
    }
    
    @Transactional
    public Document uploadDocument(MultipartFile file, String documentType) throws IOException {
        Long currentUserId = userService.getCurrentUser().getId();
        return saveDocument(currentUserId, file, documentType);
    }
    
    @Transactional
    public Document uploadDocumentForUser(Long userId, MultipartFile file, String documentType) 
            throws IOException {
        userService.getUserById(userId);
        return saveDocument(userId, file, documentType);
    }
    
    private Document saveDocument(Long userId, MultipartFile file, String documentTypeKey)
            throws IOException {
        
        
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }
        
        
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds 5MB");
        }
        
        
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && 
            !contentType.startsWith("image/"))) {
            throw new RuntimeException("Only PDF and image files are allowed");
        }
        
        
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        
        Document.DocumentType docType = DOCUMENT_TYPE_MAPPING.get(documentTypeKey);
        if (docType == null) {
            throw new RuntimeException("Invalid document type: " + documentTypeKey);
        }
        
        
        Optional<Document> existingDoc = documentRepository.findByUserIdAndDocumentType(userId, docType);
        if (existingDoc.isPresent()) {
            deletePhysicalFile(existingDoc.get().getFilePath());
            documentRepository.delete(existingDoc.get());
        }
        
        
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String uniqueFilename = UUID.randomUUID() + extension;
        
        
        Path path = Paths.get(UPLOAD_DIR, uniqueFilename);
        Files.write(path, file.getBytes());
        
       
        Document document = new Document();
        document.setUserId(userId);
        document.setDocumentType(docType);
        document.setFileName(originalFilename);
        document.setFilePath("/" + UPLOAD_DIR + uniqueFilename);
        document.setFileType(contentType);
        document.setFileSize(file.getSize());
        document.setStatus(Document.DocumentStatus.SUBMITTED);
        
        return documentRepository.save(document);
    }
    
    @Transactional
    public Document updateDocument(Long documentId, MultipartFile file) throws IOException {
        Document document = getDocumentById(documentId);
        
       
        deletePhysicalFile(document.getFilePath());
        
        
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }
        
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds 5MB");
        }
        
       
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);
        Files.write(filePath, file.getBytes());
        
        
        document.setFileName(originalFilename);
        document.setFilePath("/" + UPLOAD_DIR + uniqueFilename);
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setStatus(Document.DocumentStatus.SUBMITTED);
        document.setUploadedAt(LocalDateTime.now());
        
        return documentRepository.save(document);
    }
    
    @Transactional
    public Document updateDocumentStatus(Long documentId, String statusStr, String remarks) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        
        Document.DocumentStatus status;
        try {
            status = Document.DocumentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + statusStr + 
                ". Valid values are: SUBMITTED, PENDING, APPROVED, REJECTED");
        }
        
        User currentAdmin = userService.getCurrentUser();
        
        document.setStatus(status);
        document.setRemarks(remarks);
        
        if (status == Document.DocumentStatus.APPROVED || 
            status == Document.DocumentStatus.REJECTED) {
            document.setApprovedBy(currentAdmin.getId());
            document.setApprovedAt(LocalDateTime.now());
        }
        
        return documentRepository.save(document);
    }
    
    public ResponseEntity<Resource> downloadDocument(Long documentId) {
        Document document = getDocumentById(documentId);
        
        Path path = Paths.get("." + document.getFilePath()).normalize();
        File file = path.toFile();

        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("File not found on server");
        }
        Resource resource = new FileSystemResource(file);
        
        String contentType = document.getFileType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                   "attachment; filename=\"" + document.getFileName() + "\"")
            .body(resource);
    }
    
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = getDocumentById(documentId);
        
        
        deletePhysicalFile(document.getFilePath());
        
        
        documentRepository.delete(document);
    }
    
    @Transactional
    public void adminDeleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        
        
        deletePhysicalFile(document.getFilePath());
        
        
        documentRepository.delete(document);
    }
    
    public List<Document> getDocumentsByStatus(String statusStr) {
        Document.DocumentStatus status;
        try {
            status = Document.DocumentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + statusStr);
        }
        
        return documentRepository.findByStatusOrderByUploadedAtDesc(status);
    }
    
    private void deletePhysicalFile(String filePath) {
        try {
            File file = new File("." + filePath);
            if (file.exists()) {
                if (!file.delete()) {
                    System.err.println("Warning: Could not delete file: " + filePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + filePath + " - " + e.getMessage());
        }
    }
    
    public List<Map<String, Object>> getAllDocumentsGroupedByEmployee() {
        List<Document> allDocuments = documentRepository.findAll();
        
        Map<Long, List<Document>> documentsByUser = allDocuments.stream()
            .collect(Collectors.groupingBy(Document::getUserId));
        
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        for (Map.Entry<Long, List<Document>> entry : documentsByUser.entrySet()) {
            Long userId = entry.getKey();
            List<Document> userDocs = entry.getValue();
            
            try {
                User user = userService.getUserById(userId);
                
                if (user != null) {
                    Map<String, Object> employeeData = new HashMap<>();
                    employeeData.put("id", userId.toString());
                    employeeData.put("name", user.getFullName());
                    employeeData.put("email", user.getEmail());
                    
                    List<Map<String, Object>> documents = userDocs.stream()
                        .map(doc -> {
                            Map<String, Object> docData = new HashMap<>();
                            docData.put("id", doc.getId());
                            docData.put("documentType", doc.getDocumentType().name());
                            docData.put("fileName", doc.getFileName());
                            docData.put("fileSize", formatFileSize(doc.getFileSize()));
                            docData.put("uploadedOn", doc.getUploadedAt().format(dateFormatter));
                            docData.put("fileUrl", doc.getFilePath());
                            docData.put("status", doc.getStatus().toString());
                            return docData;
                        })
                        .collect(Collectors.toList());
                    
                    employeeData.put("documents", documents);
                    result.add(employeeData);
                }
            } catch (Exception e) {
                System.err.println("Error fetching user " + userId + ": " + e.getMessage());
            }
        }
        
        result.sort((e1, e2) -> 
            ((String) e1.get("name")).compareTo((String) e2.get("name"))
        );
        
        return result;
    }
    
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 KB";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    public String getCurrentUserPassportPhotoUrl() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String email = auth.getName();
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<Document> passportPhotoOpt =
            documentRepository.findByUserIdAndDocumentType(
                user.getId(),
                Document.DocumentType.PASSPORT_PHOTO
            );
        
        return passportPhotoOpt
            .map(Document::getFileUrl)
            .orElse(null);
    }
}
