package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.response.CourseDocumentResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseDocument;
import com.guzem.uzaktan.repository.course.CourseDocumentRepository;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.service.common.FileStorageService;
import com.guzem.uzaktan.service.course.CourseDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseDocumentServiceImpl implements CourseDocumentService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    private final CourseRepository courseRepository;
    private final CourseDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public CourseDocumentResponse upload(Long courseId, String title, MultipartFile file, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        if (course.getInstructor() == null || !course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedActionException("Bu kursa doküman yükleme yetkiniz yok.");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Lütfen bir dosya seçin.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Dosya boyutu en fazla 50 MB olabilir.");
        }

        String relativePath;
        try {
            relativePath = fileStorageService.storeCourseDocument(file, courseId, course.getTitle());
        } catch (IOException e) {
            throw new RuntimeException("Dosya kaydedilirken hata oluştu.", e);
        }

        CourseDocument doc = CourseDocument.builder()
                .course(course)
                .title(title != null && !title.isBlank() ? title : file.getOriginalFilename())
                .filePath(relativePath)
                .originalFileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .build();

        return toResponse(documentRepository.save(doc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDocumentResponse> findByCourse(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        if (course.getInstructor() == null || !course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedActionException("Bu kursun dokümanlarını görüntüleme yetkiniz yok.");
        }

        return toResponseList(documentRepository.findByCourseIdOrderByCreatedAtDesc(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDocumentResponse> findByCourse(Long courseId) {
        return toResponseList(documentRepository.findByCourseIdOrderByCreatedAtDesc(courseId));
    }

    @Override
    @Transactional
    public void delete(Long documentId, Long instructorId) {
        CourseDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Doküman", "id", documentId));

        Course course = doc.getCourse();
        if (course.getInstructor() == null || !course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedActionException("Bu dokümanı silme yetkiniz yok.");
        }

        fileStorageService.delete(doc.getFilePath());
        documentRepository.delete(doc);
    }

    private CourseDocumentResponse toResponse(CourseDocument doc) {
        return CourseDocumentResponse.builder()
                .id(doc.getId())
                .courseId(doc.getCourse().getId())
                .title(doc.getTitle())
                .filePath(doc.getFilePath())
                .originalFileName(doc.getOriginalFileName())
                .fileSize(doc.getFileSize())
                .fileType(doc.getFileType())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private List<CourseDocumentResponse> toResponseList(List<CourseDocument> docs) {
        return docs.stream().map(this::toResponse).toList();
    }
}
