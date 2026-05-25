package com.hireconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hireconnect.entity.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    
    List<Document> findByUserId(Long userId);
    
    Optional<Document> findLatestByUserIdAndDocumentType(
    	    Long userId,
    	    Document.DocumentType documentType
    	);
    
    Optional<Document> findByUserIdAndDocumentType(
    	    Long userId,
    	    Document.DocumentType documentType
    	);
    
    void deleteByUserIdAndDocumentType(
        Long userId,
        Document.DocumentType documentType
    );
    
    List<Document> findByUserIdOrderByUploadedAtDesc(Long userId);
    
    
    List<Document> findByStatus(Document.DocumentStatus status);
    
    List<Document> findByStatusOrderByUploadedAtDesc(Document.DocumentStatus status);
    
    
    List<Document> findByUserIdAndStatus(Long userId, Document.DocumentStatus status);
    
    List<Document> findByUserIdAndStatusOrderByUploadedAtDesc(
        Long userId, 
        Document.DocumentStatus status
    );
    
    
    List<Document> findAllByOrderByUploadedAtDesc();
    
    
    @Query("SELECT d FROM Document d WHERE d.status = 'SUBMITTED' OR d.status = 'PENDING' ORDER BY d.uploadedAt ASC")
    List<Document> findPendingDocuments();
    
    
    List<Document> findByApprovedBy(Long approvedBy);
    
    List<Document> findByApprovedByOrderByApprovedAtDesc(Long approvedBy);
    
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.userId = :userId AND d.status = :status")
    long countByUserIdAndStatus(
        @Param("userId") Long userId, 
        @Param("status") Document.DocumentStatus status
    );
    
   
    @Query("SELECT COUNT(d) FROM Document d WHERE d.status = :status")
    long countByStatus(@Param("status") Document.DocumentStatus status);
    
    
    @Query("SELECT d FROM Document d WHERE " +
           "LOWER(d.fileName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY d.uploadedAt DESC")
    List<Document> searchDocuments(@Param("query") String query);
    
    
    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND " +
           "LOWER(d.fileName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY d.uploadedAt DESC")
    List<Document> searchDocumentsByUser(
        @Param("userId") Long userId, 
        @Param("query") String query
    );
    
    
    @Query("SELECT d FROM Document d WHERE d.status IN ('SUBMITTED', 'PENDING') ORDER BY d.uploadedAt ASC")
    List<Document> findDocumentsNeedingApproval();
    
    
    @Query("SELECT d.status, COUNT(d) FROM Document d WHERE d.userId = :userId GROUP BY d.status")
    List<Object[]> getDocumentStatsByUser(@Param("userId") Long userId);
    
    
    @Query("SELECT d.status, COUNT(d) FROM Document d GROUP BY d.status")
    List<Object[]> getOverallDocumentStats();
}
