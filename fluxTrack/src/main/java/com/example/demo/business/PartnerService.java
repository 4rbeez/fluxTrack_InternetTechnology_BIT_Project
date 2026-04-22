package com.example.demo.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.data.domain.Partner;
import com.example.demo.data.repository.PartnerRepository;

import java.util.List;

@Service
public class PartnerService {

    @Autowired
    private PartnerRepository partnerRepository;
    
        // Get a partner by its ID
    public Partner getPartnerById(Long id) {
        try {
            Partner partner = partnerRepository.findByPartnerID(id);
            return partner;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve partner with id: " + id, e);
        }
    }

    // Get all partners
    public List<Partner> getAllPartners() {
        return partnerRepository.findAll();
    }

    // Add a new partner
    public Partner addPartner(Partner partner) throws Exception {
        if (partner.getPartnerName() == null || partner.getPartnerName().isEmpty()) {
            throw new IllegalArgumentException("Partner name cannot be null or empty");
        }
        if (partner.getPartnerEmail() == null || partner.getPartnerEmail().isEmpty()) {
            throw new IllegalArgumentException("Partner email cannot be null or empty");
        }
        else {
            return partnerRepository.save(partner);
        }
    }

    // Update an existing partner
    public Partner updatePartner(Long id, Partner updatedPartner) throws Exception {
        Partner existingPartner = partnerRepository.findByPartnerID(id);
        if (existingPartner == null) {
            throw new IllegalArgumentException("Partner with id " + id + " not found");
        }
        if (updatedPartner.getPartnerName() != null) {
            if (updatedPartner.getPartnerName().isEmpty()) {
                throw new IllegalArgumentException("Partner name cannot be empty");
            }
            existingPartner.setPartnerName(updatedPartner.getPartnerName());
        }
        if (updatedPartner.getPartnerEmail() != null) {
            if (updatedPartner.getPartnerEmail().isEmpty()) {
                throw new IllegalArgumentException("Partner email cannot be empty");
            }
            existingPartner.setPartnerEmail(updatedPartner.getPartnerEmail());
        }
        if (updatedPartner.getPartnerPhone() != null) {
            existingPartner.setPartnerPhone(updatedPartner.getPartnerPhone());
        }
        // if (updatedPartner.getPartnerAddress() != null) {
        //     existingPartner.setPartnerAddress(updatedPartner.getPartnerAddress());
        // }
        try {
            return partnerRepository.save(existingPartner);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update partner with id: " + id, e);
        }
    }

    // Delete a partner by its ID
    public void deletePartner(Long id) throws Exception {
        Partner existingPartner = partnerRepository.findByPartnerID(id);
        if (existingPartner == null) {
            throw new IllegalArgumentException("Partner with id " + id + " not found");
        }
        try {
            partnerRepository.delete(existingPartner);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete partner with id: " + id, e);
        }
    }
}
