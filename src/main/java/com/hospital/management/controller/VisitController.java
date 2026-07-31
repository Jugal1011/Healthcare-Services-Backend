package com.hospital.management.controller;

import com.hospital.management.dto.visit.ReportResponse;
import com.hospital.management.dto.visit.VisitRequest;
import com.hospital.management.dto.visit.VisitResponse;
import com.hospital.management.entity.Report;
import com.hospital.management.service.VisitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Tag(name = "Visits", description = "Patient visits — prescribed medicines and uploaded reports")
public class VisitController {

    private final VisitService visitService;

    @PostMapping
    public ResponseEntity<VisitResponse> create(@Valid @RequestBody VisitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.createVisit(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<VisitResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(visitService.getVisitsForPatient(patientId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<VisitResponse>> recent(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(visitService.getRecentVisits(limit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitResponse> update(@PathVariable Long id, @Valid @RequestBody VisitRequest request) {
        return ResponseEntity.ok(visitService.updateVisit(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Reports (file uploads) for a given visit ----

    @PostMapping(value = "/{id}/reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> uploadReport(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.addReport(id, file));
    }

    @GetMapping("/{visitId}/reports/{reportId}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long visitId, @PathVariable Long reportId) {
        Report report = visitService.getReportOrThrow(reportId);

        try {
            Path path = Path.of(report.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            report.getContentType() != null ? report.getContentType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{visitId}/reports/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long visitId, @PathVariable Long reportId) {
        visitService.deleteReport(visitId, reportId);
        return ResponseEntity.noContent().build();
    }
}
