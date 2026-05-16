package com.example.demo.business;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.data.domain.SupportTicket;
import com.example.demo.data.domain.TicketMessage;
import com.example.demo.data.domain.TicketState;
import com.example.demo.data.domain.TicketUrgency;
import com.example.demo.data.repository.SupportTicketRepository;

@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository ticketRepository;

    // -----------------------------------------------------------------
    // BUSINESS RULE (UC 107 - Submit support tickets)
    // -----------------------------------------------------------------
    // - User "admin" sees ALL tickets
    // - Partner users see ONLY their own tickets
    // -----------------------------------------------------------------
    public List<SupportTicket> getTicketsForUser(Authentication auth) {
        if (auth == null) return List.of();
        String username = auth.getName();
        if ("admin".equals(username)) {
            return ticketRepository.findAll();
        }
        Long partnerId = resolvePartnerIdFromUsername(username);
        if (partnerId == null) return List.of();
        return ticketRepository.findByPartnerID(partnerId);
    }

    public SupportTicket getTicketForUser(Long ticketId, Authentication auth) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) return null;
        if (auth == null) return null;
        String username = auth.getName();
        if ("admin".equals(username)) return ticket;
        Long callerPartnerId = resolvePartnerIdFromUsername(username);
        if (callerPartnerId == null || !callerPartnerId.equals(ticket.getPartnerID())) {
            return null;
        }
        return ticket;
    }

    // -----------------------------------------------------------------
    // Creation: any partner user (not admin) can raise a new ticket
    // for their own partner profile. The first message of the thread
    // captures the problem description from the form.
    // -----------------------------------------------------------------
    public SupportTicket createTicket(String subject, TicketUrgency urgency,
                                      String description, Authentication auth) {
        if (auth == null) {
            throw new SecurityException("Authentication required");
        }
        String username = auth.getName();
        if ("admin".equals(username)) {
            throw new SecurityException("Only partners can raise tickets");
        }
        Long partnerId = resolvePartnerIdFromUsername(username);
        if (partnerId == null) {
            throw new SecurityException("Unknown partner user");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (urgency == null) urgency = TicketUrgency.MEDIUM;

        LocalDateTime now = LocalDateTime.now();
        SupportTicket ticket = new SupportTicket();
        ticket.setPartnerID(partnerId);
        ticket.setSubject(subject);
        ticket.setUrgency(urgency);
        ticket.setState(TicketState.OPEN);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.getMessages().add(new TicketMessage(username, description, now));
        return ticketRepository.save(ticket);
    }

    // -----------------------------------------------------------------
    // BUSINESS RULE (State transition validation - UC 107 Figure 9)
    // -----------------------------------------------------------------
    // Each action below performs:
    //   1. Permission check (admin-only or partner-only or ownership)
    //   2. Allowed-from-current-state check (rejects invalid transitions)
    //   3. Adds a message to the conversation thread
    //   4. Transitions state and bumps updatedAt
    // -----------------------------------------------------------------

    /** Admin replies to an open ticket: OPEN -> ANSWERED */
    public SupportTicket adminReply(Long ticketId, String message, Authentication auth) {
        requireAdmin(auth);
        SupportTicket ticket = mustFind(ticketId);
        requireFromState(ticket, EnumSet.of(TicketState.OPEN));
        appendMessage(ticket, "admin", message);
        ticket.setState(TicketState.ANSWERED);
        return ticketRepository.save(ticket);
    }

    /** Partner replies but is not yet satisfied: ANSWERED -> OPEN */
    public SupportTicket partnerReply(Long ticketId, String message, Authentication auth) {
        String username = requirePartnerOwner(auth, mustFind(ticketId));
        SupportTicket ticket = mustFind(ticketId);
        requireFromState(ticket, EnumSet.of(TicketState.ANSWERED));
        appendMessage(ticket, username, message);
        ticket.setState(TicketState.OPEN);
        return ticketRepository.save(ticket);
    }

    /** Partner confirms problem is solved: ANSWERED -> RESOLVED */
    public SupportTicket markResolved(Long ticketId, Authentication auth) {
        String username = requirePartnerOwner(auth, mustFind(ticketId));
        SupportTicket ticket = mustFind(ticketId);
        requireFromState(ticket, EnumSet.of(TicketState.ANSWERED));
        appendMessage(ticket, username, "Problem confirmed as resolved.");
        ticket.setState(TicketState.RESOLVED);
        return ticketRepository.save(ticket);
    }

    /** Admin reopens after the partner marked resolved: RESOLVED -> ANSWERED */
    public SupportTicket adminReopen(Long ticketId, String message, Authentication auth) {
        requireAdmin(auth);
        SupportTicket ticket = mustFind(ticketId);
        requireFromState(ticket, EnumSet.of(TicketState.RESOLVED));
        appendMessage(ticket, "admin", message);
        ticket.setState(TicketState.ANSWERED);
        return ticketRepository.save(ticket);
    }

    /** Admin closes the ticket as final: RESOLVED -> COMPLETED */
    public SupportTicket markCompleted(Long ticketId, Authentication auth) {
        requireAdmin(auth);
        SupportTicket ticket = mustFind(ticketId);
        requireFromState(ticket, EnumSet.of(TicketState.RESOLVED));
        appendMessage(ticket, "admin", "Ticket closed as completed.");
        ticket.setState(TicketState.COMPLETED);
        return ticketRepository.save(ticket);
    }

    // -----------------------------------------------------------------
    // Used by application startup to insert sample tickets at various states
    // -----------------------------------------------------------------
    public SupportTicket seedTicket(Long partnerID, String subject, TicketUrgency urgency,
                                    TicketState state, LocalDateTime when,
                                    List<TicketMessage> messages) {
        SupportTicket ticket = new SupportTicket();
        ticket.setPartnerID(partnerID);
        ticket.setSubject(subject);
        ticket.setUrgency(urgency);
        ticket.setState(state);
        ticket.setCreatedAt(when);
        ticket.setUpdatedAt(when);
        ticket.getMessages().addAll(messages);
        return ticketRepository.save(ticket);
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private void requireAdmin(Authentication auth) {
        if (auth == null || !"admin".equals(auth.getName())) {
            throw new SecurityException("Admin role required");
        }
    }

    private String requirePartnerOwner(Authentication auth, SupportTicket ticket) {
        if (auth == null) throw new SecurityException("Authentication required");
        String username = auth.getName();
        if ("admin".equals(username)) {
            throw new SecurityException("This action is for partner users only");
        }
        Long callerPartnerId = resolvePartnerIdFromUsername(username);
        if (callerPartnerId == null || !callerPartnerId.equals(ticket.getPartnerID())) {
            throw new SecurityException("Cannot act on another partner's ticket");
        }
        return username;
    }

    private SupportTicket mustFind(Long ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket not found");
        }
        return ticket;
    }

    private void requireFromState(SupportTicket ticket, Set<TicketState> allowed) {
        if (!allowed.contains(ticket.getState())) {
            throw new IllegalStateException(
                "Invalid transition: ticket is in state " + ticket.getState());
        }
    }

    private void appendMessage(SupportTicket ticket, String author, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content is required");
        }
        LocalDateTime now = LocalDateTime.now();
        ticket.getMessages().add(new TicketMessage(author, content, now));
        ticket.setUpdatedAt(now);
    }

    private Long resolvePartnerIdFromUsername(String username) {
        if (username == null) return null;
        switch (username) {
            case "wylaade":      return 1L;
            case "drachehoehli": return 2L;
            default:             return null;
        }
    }
}
