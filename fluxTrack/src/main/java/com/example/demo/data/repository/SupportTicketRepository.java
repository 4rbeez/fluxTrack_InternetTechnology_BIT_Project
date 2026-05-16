package com.example.demo.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.data.domain.SupportTicket;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    /**
     * Returns all tickets raised by a specific partner.
     * Used by SupportTicketService.getTicketsForUser() to enforce role-based filtering.
     */
    List<SupportTicket> findByPartnerID(Long partnerID);
}
