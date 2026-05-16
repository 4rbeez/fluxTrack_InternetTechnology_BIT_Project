package com.example.demo.data.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/**
 * SupportTicket entity (UC 107 — Submit support tickets).
 *
 * A ticket is raised by a partner, optionally responded to by admin (fluxed),
 * and progresses through the state machine defined in TicketState.
 *
 * The conversation between partner and admin is stored as a list of
 * TicketMessage values (embeddable, child table "ticket_message").
 */
@Entity
@Table(name = "support_ticket")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ticket_id", nullable = false)
    private Long ticketID;

    @Column(name = "partner_id", nullable = false)
    private Long partnerID;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false)
    private TicketUrgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private TicketState state;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "ticket_message",
        joinColumns = @JoinColumn(name = "ticket_id")
    )
    @OrderColumn(name = "message_index")
    private List<TicketMessage> messages = new ArrayList<>();

    public SupportTicket() {}

    public Long getTicketID() { return ticketID; }
    public void setTicketID(Long ticketID) { this.ticketID = ticketID; }

    public Long getPartnerID() { return partnerID; }
    public void setPartnerID(Long partnerID) { this.partnerID = partnerID; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public TicketUrgency getUrgency() { return urgency; }
    public void setUrgency(TicketUrgency urgency) { this.urgency = urgency; }

    public TicketState getState() { return state; }
    public void setState(TicketState state) { this.state = state; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<TicketMessage> getMessages() { return messages; }
    public void setMessages(List<TicketMessage> messages) { this.messages = messages; }
}
