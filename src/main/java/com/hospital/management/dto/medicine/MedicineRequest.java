package com.hospital.management.dto.medicine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MedicineRequest {

    @NotBlank
    private String name;

    private String manufacturer;

    private String composition;

    private String dosageForm;

    @PositiveOrZero
    private Double price;

    private String description;
}
