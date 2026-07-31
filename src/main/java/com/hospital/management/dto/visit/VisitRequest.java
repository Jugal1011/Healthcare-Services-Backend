package com.hospital.management.dto.visit;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VisitRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private LocalDate visitDate;

    private String notes;

    // IDs of medicines prescribed during this visit
    @NotEmpty
    private List<Long> medicineIds;
}
