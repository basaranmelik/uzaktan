package com.guzem.uzaktan.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    String store(MultipartFile file, Long assignmentId) throws IOException;

    String storeVideo(MultipartFile file, Long courseId) throws IOException;

    String storeImage(MultipartFile file) throws IOException;

    Path resolve(String relativePath);

    void delete(String relativePath);
}
