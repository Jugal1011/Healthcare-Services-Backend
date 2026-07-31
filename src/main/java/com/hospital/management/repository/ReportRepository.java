package com.hospital.management.repository;

import com.hospital.management.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByVisitId(Long visitId);
}
