package com.hospital.management.dto.visit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime uploadedAt;
}
