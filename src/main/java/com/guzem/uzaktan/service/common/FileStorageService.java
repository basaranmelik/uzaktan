package com.guzem.uzaktan.service.common;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    String store(MultipartFile file, Long assignmentId, String courseTitle) throws IOException;

    String storeWithName(MultipartFile file, Long assignmentId, String courseTitle, String baseName) throws IOException;

    String storeVideo(MultipartFile file, Long courseId) throws IOException;

    // New organized storage methods
    String storeVideo(MultipartFile file, Long courseId, String courseTitle, String videoTitle) throws IOException;

    String storeImage(MultipartFile file) throws IOException;

    String storeCourseImage(MultipartFile file, Long courseId) throws IOException;

    String storeCourseImage(MultipartFile file, Long courseId, String courseTitle) throws IOException;

    Path resolve(String relativePath);

    void delete(String relativePath);
}
