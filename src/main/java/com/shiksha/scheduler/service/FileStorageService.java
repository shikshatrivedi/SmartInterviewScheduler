package com.shiksha.scheduler.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/resumes/";

    public String storeResume(MultipartFile file, Long candidateId) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = file.getOriginalFilename();
        String extension = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".pdf";
        String storedName = "candidate_" + candidateId + "_" + UUID.randomUUID() + extension;

        Path targetPath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return storedName;
    }

    public byte[] loadResume(String filename) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
        return Files.readAllBytes(filePath);
    }

    public boolean resumeExists(String filename) {
        if (filename == null || filename.isBlank()) return false;
        return Files.exists(Paths.get(UPLOAD_DIR).resolve(filename));
    }
}
