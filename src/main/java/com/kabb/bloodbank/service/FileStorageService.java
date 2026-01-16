package com.kabb.bloodbank.service;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${file.upload.license-dir}")
    private String licenseUploadDir;

    /**
     * 면허증 파일 저장
     * @param file 업로드된 파일
     * @return 저장된 파일 정보 (저장 경로, 파일명 등)
     */
    public FileStorageResult storeLicenseFile(MultipartFile file) throws IOException {
        // 파일 유효성 검사
        validateFile(file);

        // 디렉토리 생성
        Path uploadPath = Paths.get(licenseUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 고유한 파일명 생성 (UUID 사용)
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storedFileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(storedFileName);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return FileStorageResult.builder()
                .originalFileName(originalFilename)
                .storedFileName(storedFileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 검사 (10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다");
        }

        // 파일 타입 검사 (이미지 또는 PDF)
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("이미지 파일 또는 PDF 파일만 업로드 가능합니다");
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    /**
     * 파일 저장 결과 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class FileStorageResult {
        private String originalFileName;
        private String storedFileName;
        private String filePath;
        private long fileSize;
        private String contentType;
    }
}

