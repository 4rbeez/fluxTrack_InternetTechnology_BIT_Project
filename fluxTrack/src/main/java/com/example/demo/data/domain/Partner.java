package com.example.demo.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;

/**
 * Partner entity matching the API schema in fluxTrack-API-0.0.2.yaml.
 */
@Entity
@Table(name = "partner")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "partner_id", nullable = false)
    private Long partnerID;

    @Column(name = "partner_name", nullable = false)
    private String partnerName;

    @Column(name = "partner_email", nullable = false)
    private String partnerEmail;

    @Column(name = "partner_phone")
    private String partnerPhone;

    @ElementCollection
    private List<Address> partnerAddress;

    // One-to-Many relationship with Product
    @OneToMany(mappedBy = "product_partner_id")
    private List<Product> productList;

    // Empty Constructor for JPA
    public Partner() {
    }

    // Full Constructor - might not be needed
    public Partner(Long partnerID, String partnerName, String partnerEmail, String partnerPhone, List<Address> partnerAddress) {
        this.partnerID = partnerID;
        this.partnerName = partnerName;
        this.partnerEmail = partnerEmail;
        this.partnerPhone = partnerPhone;
        this.partnerAddress = partnerAddress;
    }

    // Getters and Setters
    public Long getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(Long partnerID) {
        this.partnerID = partnerID;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerEmail() {
        return partnerEmail;
    }

    public void setPartnerEmail(String partnerEmail) {
        this.partnerEmail = partnerEmail;
    }

    public String getPartnerPhone() {
        return partnerPhone;
    }

    public void setPartnerPhone(String partnerPhone) {
        this.partnerPhone = partnerPhone;
    }

    public List<Address> getPartnerAddress() {
        return partnerAddress;
    }

    public void setPartnerAddress(List<Address> partnerAddress) {
        this.partnerAddress = partnerAddress;
    }
}
