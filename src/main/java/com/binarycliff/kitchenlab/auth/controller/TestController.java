package com.binarycliff.kitchenlab.auth.controller;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Test controller for debugging authentication issues.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/admin-info")
    public ResponseEntity<Map<String, Object>> getAdminInfo() {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Admin> adminOpt = adminRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            response.put("username", admin.getUsername());
            response.put("email", admin.getEmail());
            response.put("passwordHash", admin.getPassword());
            response.put("isEnabled", admin.getIsEnabled());
            response.put("passwordMatches", passwordEncoder.matches("admin123", admin.getPassword()));
            response.put("testHash", passwordEncoder.encode("admin123"));
        } else {
            response.put("error", "Admin user not found");
        }
        
        return ResponseEntity.ok(response);
    }
}
