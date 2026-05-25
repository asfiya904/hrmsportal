package com.hireconnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.dto.request.TicketRequest;
import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.entity.Ticket;
import com.hireconnect.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:3000"},
	    allowCredentials = "true"
	)
public class TicketController {
    
    private final TicketService ticketService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Ticket>> createTicket(@RequestBody TicketRequest request) {
        try {
            Ticket ticket = ticketService.createTicket(request);
            return ResponseEntity.ok(ApiResponse.success("Ticket created", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<List<Ticket>>> getMyTickets() {
        try {
            List<Ticket> tickets = ticketService.getMyTickets();
            return ResponseEntity.ok(ApiResponse.success("Tickets fetched", tickets));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Ticket>>> getAllTickets() {
        try {
            List<Ticket> tickets = ticketService.getAllTickets();
            return ResponseEntity.ok(ApiResponse.success("All tickets fetched", tickets));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Ticket>> getTicketById(@PathVariable Long id) {
        try {
            Ticket ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ApiResponse.success("Ticket fetched", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Ticket>> updateTicketStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String status = (String) request.get("status");
            Long assignedToId = request.get("assignedToId") != null ? 
                Long.parseLong(request.get("assignedToId").toString()) : null;
            
            Ticket ticket = ticketService.updateTicketStatus(id, status, assignedToId);
            return ResponseEntity.ok(ApiResponse.success("Ticket status updated", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/comment")
    public ResponseEntity<ApiResponse<String>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String comment = request.get("comment");
            ticketService.addComment(id, comment);
            return ResponseEntity.ok(ApiResponse.success("Comment added", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getTicketStats() {
        try {
            var stats = ticketService.getTicketStats();
            return ResponseEntity.ok(ApiResponse.success("Stats fetched", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}