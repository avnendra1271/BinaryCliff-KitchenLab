package com.binarycliff.kitchenlab.auth.dto;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing admin user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminRequest {
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String phoneNumber;
    
    private Admin.AdminRole role;
    
    private Boolean isActive;
    
    private String profileImageUrl;
}
