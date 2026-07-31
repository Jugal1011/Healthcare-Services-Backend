package com.hospital.management.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;

    @Email
    private String email;

    private String mobileNumber;

    // Only ADMIN can change role/enabled via the admin endpoint.
    private String role;
    private Boolean enabled;
}
