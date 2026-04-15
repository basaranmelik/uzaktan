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
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS =
            Set.of(".mp4", ".webm", ".avi", ".mkv", ".mov");

    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS =
            Set.of(".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".txt", ".zip");

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

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
    public String store(MultipartFile file, Long assignmentId, String courseTitle) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), "");
        validateExtension(extension, ALLOWED_DOCUMENT_EXTENSIONS, "ödev");
        String storedName = generateFileName("sub", extension);
        String courseSlug = sanitizeFileName(courseTitle);
        return storeFile(file, "odevler/" + courseSlug + "_" + assignmentId + "/" + storedName);
    }

    @Override
    public String storeWithName(MultipartFile file, Long assignmentId, String courseTitle, String baseName) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), "");
        validateExtension(extension, ALLOWED_DOCUMENT_EXTENSIONS, "ödev");
        String storedName = sanitizeFileName(baseName) + "_" + (System.currentTimeMillis() % 100000) + extension;
        String courseSlug = sanitizeFileName(courseTitle);
        return storeFile(file, "odevler/" + courseSlug + "_" + assignmentId + "/" + storedName);
    }

    @Override
    public String storeVideo(MultipartFile file, Long courseId) throws IOException {
        return storeVideo(file, courseId, "course", "video");
    }

    @Override
    public String storeVideo(MultipartFile file, Long courseId, String courseTitle, String videoTitle) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), ".mp4");
        validateExtension(extension, ALLOWED_VIDEO_EXTENSIONS, "video");

        String courseSlug = sanitizeFileName(courseTitle) + "_" + courseId;
        String videoSlug = sanitizeFileName(videoTitle);
        String finalFileName = videoSlug + "_" + (System.currentTimeMillis() % 100000) + extension;

        return storeFile(file, "videolar/" + courseSlug + "/" + finalFileName);
    }

    @Override
    public String storeImage(MultipartFile file) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), ".jpg");
        validateExtension(extension, ALLOWED_IMAGE_EXTENSIONS, "görsel");
        String storedName = generateFileName("img", extension);
        return storeFile(file, "images/egitmenler/" + storedName);
    }

    @Override
    public String storeCourseImage(MultipartFile file, Long courseId) throws IOException {
        return storeCourseImage(file, courseId, "course");
    }

    @Override
    public String storeCourseImage(MultipartFile file, Long courseId, String courseTitle) throws IOException {
        String extension = extractExtension(file.getOriginalFilename(), ".jpg");
        validateExtension(extension, ALLOWED_IMAGE_EXTENSIONS, "görsel");

        String courseSlug = sanitizeFileName(courseTitle) + "_" + courseId;
        String finalFileName = "kapak_" + (System.currentTimeMillis() % 100000) + extension;

        return storeFile(file, "images/kurslar/" + courseSlug + "/" + finalFileName);
    }

    @Override
    public Path resolve(String relativePath) {
        Path resolved = baseDir.resolve(relativePath).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new SecurityException("Path traversal girişimi engellendi: " + relativePath);
        }
        return resolved;
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
        Path target = baseDir.resolve(relativePath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new SecurityException("Path traversal girişimi engellendi: " + relativePath);
        }
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
                ? clean.substring(clean.lastIndexOf('.')).toLowerCase()
                : defaultExtension;
    }

    private void validateExtension(String extension, Set<String> allowed, String fileType) {
        if (!allowed.contains(extension)) {
            throw new IllegalArgumentException(
                    "Geçersiz " + fileType + " formatı. İzin verilen formatlar: " + allowed);
        }
    }
}
