package com.hospital.management.dto.visit;

import com.hospital.management.dto.medicine.MedicineResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private LocalDate visitDate;
    private String notes;
    private List<MedicineResponse> medicinesPrescribed;
    private List<ReportResponse> reports;
}
