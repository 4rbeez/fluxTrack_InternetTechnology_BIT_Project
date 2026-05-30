package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.business.AppUserService;
import com.example.demo.business.PartnerService;
import com.example.demo.data.domain.AppUser;
import com.example.demo.data.domain.Partner;

@RestController
@RequestMapping("/partner")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private AppUserService appUserService;

    // Get Partner by ID
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<Partner> getPartnerById(@PathVariable Long id) {
        try {
            Partner partner = partnerService.getPartnerById(id);
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found with given id: " + id, e);
        }
    }

    // Get All Partners
    @GetMapping(path = "/", produces = "application/json")
    public List<Partner> getAllPartners() {
        return partnerService.getAllPartners();
    }

    // Add Partner
    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Partner> addPartner(@RequestBody Partner partner) {
        try {
            partner = partnerService.addPartner(partner);
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to add partner: " + e.getMessage(), e);
        }
    }

    // Update Partner
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Partner> updatePartner(@PathVariable Long id, @RequestBody Partner partner) {
        try {
            partner = partnerService.updatePartner(id, partner);
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found with given id: " + id, e);
        }
    }

    // Delete Partner
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        // Block deletion if any user accounts are linked to this partner
        List<AppUser> linkedUsers = appUserService.getAllUsers().stream()
                .filter(u -> id.equals(u.getPartnerID()))
                .toList();
        if (!linkedUsers.isEmpty()) {
            String usernames = linkedUsers.stream()
                    .map(AppUser::getUsername)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete partner: linked user account(s) exist (" + usernames + "). Remove or reassign them first.");
        }

        try {
            partnerService.deletePartner(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found with given id: " + id, e);
        }
    }
}