package com.hospital.management.service;

import com.hospital.management.dto.medicine.MedicineResponse;
import com.hospital.management.dto.visit.ReportResponse;
import com.hospital.management.dto.visit.VisitRequest;
import com.hospital.management.dto.visit.VisitResponse;
import com.hospital.management.entity.Medicine;
import com.hospital.management.entity.Patient;
import com.hospital.management.entity.Report;
import com.hospital.management.entity.Visit;
import com.hospital.management.exception.BadRequestException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.repository.MedicineRepository;
import com.hospital.management.repository.PatientRepository;
import com.hospital.management.repository.ReportRepository;
import com.hospital.management.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final MedicineRepository medicineRepository;
    private final ReportRepository reportRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public VisitResponse createVisit(VisitRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        List<Medicine> medicines = medicineRepository.findAllById(request.getMedicineIds());
        if (medicines.size() != request.getMedicineIds().size()) {
            throw new BadRequestException("One or more medicine IDs are invalid");
        }

        Visit visit = Visit.builder()
                .patient(patient)
                .visitDate(request.getVisitDate())
                .notes(request.getNotes())
                .medicinesPrescribed(medicines)
                .build();

        return toResponse(visitRepository.save(visit));
    }

    public VisitResponse getVisitById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsForPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        List<Visit> visits = visitRepository.findByPatientIdOrderByVisitDateDesc(patientId);

        return visits.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VisitResponse updateVisit(Long id, VisitRequest request) {
        Visit visit = findOrThrow(id);

        if (!visit.getPatient().getId().equals(request.getPatientId())) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
            visit.setPatient(patient);
        }

        List<Medicine> medicines = medicineRepository.findAllById(request.getMedicineIds());
        if (medicines.size() != request.getMedicineIds().size()) {
            throw new BadRequestException("One or more medicine IDs are invalid");
        }

        visit.setVisitDate(request.getVisitDate());
        visit.setNotes(request.getNotes());
        visit.setMedicinesPrescribed(medicines);

        return toResponse(visitRepository.save(visit));
    }

    @Transactional
    public void deleteVisit(Long id) {
        Visit visit = findOrThrow(id);
        // Clean up any physical report files before deleting the DB rows
        visit.getReports().forEach(r -> fileStorageService.delete(r.getFilePath()));
        visitRepository.delete(visit);
    }

    @Transactional
    public ReportResponse addReport(Long visitId, MultipartFile file) {
        Visit visit = findOrThrow(visitId);

        FileStorageService.StoredFile stored = fileStorageService.store(file);

        Report report = Report.builder()
                .visit(visit)
                .fileName(stored.originalName())
                .storedFileName(stored.storedName())
                .filePath(stored.path())
                .contentType(stored.contentType())
                .fileSize(stored.size())
                .build();

        report = reportRepository.save(report);
        return toReportResponse(report);
    }

    @Transactional
    public void deleteReport(Long visitId, Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        if (!report.getVisit().getId().equals(visitId)) {
            throw new BadRequestException("Report does not belong to the specified visit");
        }

        fileStorageService.delete(report.getFilePath());
        reportRepository.delete(report);
    }

    public Report getReportOrThrow(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
    }

    public List<VisitResponse> getRecentVisits(int limit) {
        // Use Pageable to query recent visits sorted by visitDate desc
        var pageable = org.springframework.data.domain.PageRequest.of(0, Math.max(1, limit), org.springframework.data.domain.Sort.by("visitDate").descending());
        return visitRepository.findAll(pageable).stream().map(this::toResponse).toList();
    }

    private Visit findOrThrow(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + id));
    }

    private VisitResponse toResponse(Visit visit) {
        List<MedicineResponse> medicines = visit.getMedicinesPrescribed().stream()
                .map(m -> MedicineResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .manufacturer(m.getManufacturer())
                        .composition(m.getComposition())
                        .dosageForm(m.getDosageForm())
                        .price(m.getPrice())
                        .description(m.getDescription())
                        .build())
                .toList();

        List<ReportResponse> reports = visit.getReports().stream()
                .map(this::toReportResponse)
                .toList();

        return VisitResponse.builder()
                .id(visit.getId())
                .patientId(visit.getPatient().getId())
                .patientName(visit.getPatient().getName())
                .visitDate(visit.getVisitDate())
                .notes(visit.getNotes())
                .medicinesPrescribed(medicines)
                .reports(reports)
                .build();
    }

    private ReportResponse toReportResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .fileName(report.getFileName())
                .contentType(report.getContentType())
                .fileSize(report.getFileSize())
                .downloadUrl("/api/visits/" + report.getVisit().getId() + "/reports/" + report.getId() + "/download")
                .uploadedAt(report.getUploadedAt())
                .build();
    }
}
