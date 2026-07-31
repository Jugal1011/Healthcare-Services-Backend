package com.hospital.management.service;

import com.hospital.management.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local filesystem storage for report files — free, no third-party account needed.
 * For production/multi-instance deployments, swap this out for a cloud object store
 * (e.g. AWS S3 free tier / Cloudflare R2 free tier) behind the same interface.
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file-storage.reports-dir}")
    private String reportsDir;

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        try {
            Path uploadPath = Paths.get(reportsDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalName = Path.of(file.getOriginalFilename()).getFileName().toString();
            String storedName = UUID.randomUUID() + "_" + originalName;
            Path targetPath = uploadPath.resolve(storedName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(originalName, storedName, targetPath.toString(),
                    file.getContentType(), file.getSize());
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BadRequestException("Failed to store uploaded file: " + e.getMessage());
        }
    }

    public Path resolve(String filePath) {
        return Paths.get(filePath).normalize();
    }

    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath, e);
        }
    }

    public record StoredFile(String originalName, String storedName, String path,
                              String contentType, long size) {}
}
