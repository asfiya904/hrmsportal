package com.hireconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hireconnect.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Employee view – tickets raised by employee (latest first)
     */
    List<Ticket> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    /**
     * Admin view – all tickets (latest first)
     */
    List<Ticket> findAllByOrderByCreatedAtDesc();

    /**
     * Filter tickets by status
     */
    List<Ticket> findByStatus(Ticket.TicketStatus status);

    /**
     * Dashboard metric – count tickets by status
     */
    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.status = :status
    """)
    long countByStatus(
        @Param("status") Ticket.TicketStatus status
    );
}
