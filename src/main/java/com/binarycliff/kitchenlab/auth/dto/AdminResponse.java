package com.binarycliff.kitchenlab.auth.dto;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for admin user response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profileImageUrl;
    private Admin.AdminRole role;
    private List<String> permissions;
    private Boolean isActive;
    private String phoneNumber;
}
