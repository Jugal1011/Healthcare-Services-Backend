package com.hospital.management.dto.patient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String contact;

    @Valid
    private AddressDto address;
}
