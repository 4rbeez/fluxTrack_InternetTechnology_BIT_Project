package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.business.AppUserService;
import com.example.demo.data.domain.AppUser;

@RestController
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * Returns the authenticated user's profile.
     * Called by auth.js immediately after login to cache display name + logo.
     */
    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication auth) {
        AppUser user = appUserService.findByUsername(auth.getName());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("displayName", user.getDisplayName());
        profile.put("role", user.getRole());
        profile.put("partnerID", user.getPartnerID());

        // Resolve avatar URL: uploaded avatar > static logo > null
        if (user.getAvatar() != null && user.getAvatar().length > 0) {
            profile.put("avatarUrl", "/user/" + user.getId() + "/avatar");
        } else {
            profile.put("avatarUrl", user.getLogoPath()); // may be null — frontend handles fallback
        }

        return ResponseEntity.ok(profile);
    }
}