package com.goorm.team9.icontact.domain.client.service;

import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ImageStorageService {

    private final S3Template s3Template;

    @Value("${cloud.aws.s3.default-image}")
    private String defaultImage;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    private static final String DEFAULT_IMAGE = "https://i-contact-s3.s3.ap-northeast-2.amazonaws.com/default_profile_image.jpg";
    private static final String PROFILE_IMAGE_FOLDER = "profile-images/";


    public String storeFile(MultipartFile file) {
        try {
            // 파일명을 UUID로 변환하여 중복 방지
            String uniqueFileName = PROFILE_IMAGE_FOLDER + UUID.randomUUID() + "_" + file.getOriginalFilename();

            try (InputStream is = file.getInputStream()) {
                // S3에 파일 업로드
                S3Resource upload = s3Template.upload(bucketName, uniqueFileName, is);
                return upload.getURL().toString(); // 업로드된 S3 URL 반환
            }
        } catch (IOException | S3Exception e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || isDefaultImage(fileUrl)) {
            return;
        }

        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            s3Template.deleteObject(bucketName, PROFILE_IMAGE_FOLDER + fileName);
        } catch (S3Exception e) {
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }

    public boolean isDefaultImage(String fileUrl) {
        return fileUrl == null || fileUrl.equals(DEFAULT_IMAGE);
    }

    public String getDefaultImage() {
        return defaultImage;
    }
}