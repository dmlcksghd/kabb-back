package com.kabb.bloodbank.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeLicenseFileSavesPdf() throws IOException {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "licenseUploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "licenseFile",
                "license.pdf",
                "application/pdf",
                "test".getBytes()
        );

        FileStorageService.FileStorageResult result = service.storeLicenseFile(file);

        Path storedPath = Path.of(result.getFilePath());
        assertTrue(Files.exists(storedPath));
        assertEquals("license.pdf", result.getOriginalFileName());
        assertEquals("application/pdf", result.getContentType());
        assertEquals(4, result.getFileSize());
    }

    @Test
    void storeLicenseFileRejectsLargeFile() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "licenseUploadDir", tempDir.toString());

        byte[] largeContent = new byte[(10 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "licenseFile",
                "license.pdf",
                "application/pdf",
                largeContent
        );

        assertThrows(IllegalArgumentException.class, () -> service.storeLicenseFile(file));
    }

    @Test
    void storeLicenseFileRejectsInvalidContentType() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "licenseUploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "licenseFile",
                "license.txt",
                "text/plain",
                "test".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> service.storeLicenseFile(file));
    }
}
