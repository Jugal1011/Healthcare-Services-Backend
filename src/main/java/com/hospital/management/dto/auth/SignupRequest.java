package com.hospital.management.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Mobile number must be 10-15 digits")
    private String mobileNumber;

    // Optional - only an existing ADMIN calling this should be able to set role=ADMIN.
    // Default handled in service layer.
    private String role;
}
