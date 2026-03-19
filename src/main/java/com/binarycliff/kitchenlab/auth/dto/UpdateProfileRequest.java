package com.binarycliff.kitchenlab.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating admin profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    private String firstName;
    
    private String lastName;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String phoneNumber;
    
    private String profileImageUrl;
    
    @Size(max = 500, message = "Bio must be less than 500 characters")
    private String bio;
}
