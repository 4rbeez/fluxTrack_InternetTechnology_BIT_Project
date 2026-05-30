package com.example.demo.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.data.domain.AppUser;
import com.example.demo.data.repository.AppUserRepository;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---- Spring Security integration ----

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        // Every user gets ROLE_PARTNER — required by SecurityFilterChain for POST /token
        authorities.add(new SimpleGrantedAuthority("ROLE_PARTNER"));
        if ("ADMIN".equals(user.getRole())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }

    // ---- Business methods ----

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    public AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username).orElse(null);
    }

    public AppUser findById(Long id) {
        return appUserRepository.findById(id).orElse(null);
    }

    public boolean existsByUsername(String username) {
        return appUserRepository.existsByUsername(username);
    }

    /** Seed a user with a raw (plain-text) password — BCrypt encoding happens here. */
    public AppUser seedUser(String username, String rawPassword, String role,
                            Long partnerID, String displayName, String logoPath) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setPartnerID(partnerID);
        user.setDisplayName(displayName);
        user.setLogoPath(logoPath);
        return appUserRepository.save(user);
    }

    public AppUser saveUser(AppUser user) {
        return appUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        appUserRepository.deleteById(id);
    }

    /** Shared admin check — replaces all hardcoded "admin".equals(username) checks. */
    public boolean isAdminUser(String username) {
        AppUser user = appUserRepository.findByUsername(username).orElse(null);
        return user != null && "ADMIN".equals(user.getRole());
    }

    /** Shared partnerID resolution — replaces all hardcoded switch maps. */
    public Long getPartnerIdForUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username).orElse(null);
        return user != null ? user.getPartnerID() : null;
    }
}