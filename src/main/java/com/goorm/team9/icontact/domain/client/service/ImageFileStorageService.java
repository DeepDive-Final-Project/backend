package com.goorm.team9.icontact.domain.client.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageFileStorageService {
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profile-images/";
    private static final String DEFAULT_IMAGE = "/profile-images/default_profile_image.jpg";

    public String storeFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/profile-images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || isDefaultImage(filePath)) {
            return;
        }
        try {
            Path fileToDeletePath = Paths.get(UPLOAD_DIR).resolve(filePath.substring("/profile-images/".length())).toAbsolutePath();
            Files.deleteIfExists(fileToDeletePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }

    public boolean isDefaultImage(String filePath) {
        return filePath == null || filePath.endsWith("default_profile_image.jpg");
    }
}

