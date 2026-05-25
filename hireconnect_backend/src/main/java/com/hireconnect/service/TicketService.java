package com.hireconnect.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.dto.request.TicketRequest;
import com.hireconnect.entity.Ticket;
import com.hireconnect.entity.User;
import com.hireconnect.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
    
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public Ticket createTicket(TicketRequest request) {
        User currentUser = userService.getCurrentUser();
        
        Ticket ticket = new Ticket();
        ticket.setEmployeeId(currentUser.getId());
        ticket.setEmployeeName(currentUser.getFullName());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority() != null ? 
            Ticket.TicketPriority.valueOf(request.getPriority().toUpperCase()) : 
            Ticket.TicketPriority.MEDIUM);
        ticket.setStatus(Ticket.TicketStatus.OPEN);
        
        return ticketRepository.save(ticket);
    }
    
    public List<Ticket> getMyTickets() {
        User currentUser = userService.getCurrentUser();
        return ticketRepository.findByEmployeeIdOrderByCreatedAtDesc(currentUser.getId());
    }
    
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }
    
    @Transactional
    public Ticket updateTicketStatus(Long id, String status, Long assignedToId) {
        Ticket ticket = getTicketById(id);
        
        ticket.setStatus(Ticket.TicketStatus.valueOf(status.toUpperCase()));
        if (assignedToId != null) {
            ticket.setAssignedToId(assignedToId);
        }
        
        return ticketRepository.save(ticket);
    }
    
    @Transactional
    public void addComment(Long ticketId, String commentText) {
        Ticket ticket = getTicketById(ticketId);
        User currentUser = userService.getCurrentUser();
        
        // Parse existing comments
        List<Map<String, Object>> comments = parseComments(ticket.getComments());
        
        // Add new comment
        Map<String, Object> newComment = new HashMap<>();
        newComment.put("id", System.currentTimeMillis());
        newComment.put("authorId", currentUser.getId());
        newComment.put("authorName", currentUser.getFullName());
        newComment.put("authorType", currentUser.getRole().name());
        newComment.put("comment", commentText);
        newComment.put("createdAt", new java.util.Date());
        
        comments.add(newComment);
        
        // Save back to ticket
        try {
            ticket.setComments(objectMapper.writeValueAsString(comments));
            ticketRepository.save(ticket);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error adding comment", e);
        }
    }
    
    public Map<String, Long> getTicketStats() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("total", ticketRepository.count());
        stats.put("open", ticketRepository.countByStatus(Ticket.TicketStatus.OPEN));
        stats.put("in_progress", ticketRepository.countByStatus(Ticket.TicketStatus.IN_PROGRESS));
        stats.put("resolved", ticketRepository.countByStatus(Ticket.TicketStatus.RESOLVED));
        stats.put("closed", ticketRepository.countByStatus(Ticket.TicketStatus.CLOSED));
        
        return stats;
    }
    
    private List<Map<String, Object>> parseComments(String commentsJson) {
        if (commentsJson == null || commentsJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(commentsJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}