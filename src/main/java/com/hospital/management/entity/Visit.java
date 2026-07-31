package com.hospital.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"visits", "hibernateLazyInitializer", "handler"})
    private Patient patient;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    private String notes;

    // A visit can prescribe multiple medicines; a medicine can appear across many visits.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "visit_medicines",
            joinColumns = @JoinColumn(name = "visit_id"),
            inverseJoinColumns = @JoinColumn(name = "medicine_id")
    )
    @Builder.Default
    private List<Medicine> medicinesPrescribed = new ArrayList<>();

    // A visit can have multiple uploaded report files (lab reports, scans, etc.)
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"visit", "hibernateLazyInitializer", "handler"})
    @Builder.Default
    private List<Report> reports = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
