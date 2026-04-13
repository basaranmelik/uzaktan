package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final Path baseDir;

    public LocalFileStorageService(
            @Value("${app.upload.dir:${user.home}/guzem-uploads}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Yükleme dizini oluşturulamadı: " + uploadDir, e);
        }
    }

    @Override
    public String store(MultipartFile file, Long assignmentId) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), "");
        String storedName = generateFileName("submission", extension);
        return storeFile(file, "odevler/" + assignmentId + "/" + storedName);
    }

    @Override
    public String storeWithName(MultipartFile file, Long assignmentId, String baseName) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), "");
        String storedName = sanitizeFileName(baseName) + extension;
        return storeFile(file, "odevler/" + assignmentId + "/" + storedName);
    }

    @Override
    public String storeVideo(MultipartFile file, Long courseId) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), ".mp4");
        String storedName = generateFileName("video", extension);
        return storeFile(file, "videolar/" + courseId + "/" + storedName);
    }

    @Override
    public String storeImage(MultipartFile file) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), ".jpg");
        String storedName = generateFileName("img", extension);
        return storeFile(file, "images/egitmenler/" + storedName);
    }

    @Override
    public Path resolve(String relativePath) {
        return baseDir.resolve(relativePath).normalize();
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException e) {
            log.warn("Dosya silinirken hata: {} - {}", relativePath, e.getMessage());
        }
    }

    private String storeFile(MultipartFile file, String relativePath) throws IOException {
        Path target = baseDir.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return relativePath;
    }

    private String generateFileName(String prefix, String extension) {
        return prefix + "_" + System.currentTimeMillis()
                + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    public static String sanitizeFileName(String input) {
        if (input == null) return "dosya";
        String s = input.toLowerCase(java.util.Locale.forLanguageTag("tr"))
                .replace('ç', 'c').replace('ğ', 'g').replace('ı', 'i')
                .replace('ö', 'o').replace('ş', 's').replace('ü', 'u')
                .replace('â', 'a').replace('î', 'i').replace('û', 'u');
        return s.replaceAll("[^a-z0-9_]+", "_").replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private String extractExtension(String originalFilename, String defaultExtension) {
        String clean = StringUtils.cleanPath(originalFilename != null ? originalFilename : "file");
        return clean.contains(".")
                ? clean.substring(clean.lastIndexOf('.'))
                : defaultExtension;
    }
}
