package com.hospital.management.service;

import com.hospital.management.dto.medicine.MedicineRequest;
import com.hospital.management.dto.medicine.MedicineResponse;
import com.hospital.management.entity.Medicine;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;

    @Transactional
    public MedicineResponse createMedicine(MedicineRequest request) {
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .manufacturer(request.getManufacturer())
                .composition(request.getComposition())
                .dosageForm(request.getDosageForm())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();
        return toResponse(medicineRepository.save(medicine));
    }

    public Page<MedicineResponse> getAllMedicines(Pageable pageable) {
        return medicineRepository.findAll(pageable).map(this::toResponse);
    }

    public MedicineResponse getMedicineById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public java.util.List<MedicineResponse> searchByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name).stream().map(this::toResponse).toList();
    }

    @Transactional
    public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = findOrThrow(id);
        medicine.setName(request.getName());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setComposition(request.getComposition());
        medicine.setDosageForm(request.getDosageForm());
        medicine.setPrice(request.getPrice());
        medicine.setDescription(request.getDescription());
        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public void deleteMedicine(Long id) {
        medicineRepository.delete(findOrThrow(id));
    }

    Medicine findOrThrow(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
    }

    MedicineResponse toResponse(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .manufacturer(medicine.getManufacturer())
                .composition(medicine.getComposition())
                .dosageForm(medicine.getDosageForm())
                .price(medicine.getPrice())
                .description(medicine.getDescription())
                .build();
    }
}
