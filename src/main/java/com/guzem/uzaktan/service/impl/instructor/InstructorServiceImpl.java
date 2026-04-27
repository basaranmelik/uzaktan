package com.guzem.uzaktan.service.impl.instructor;

import com.guzem.uzaktan.dto.request.InstructorCreateRequest;
import com.guzem.uzaktan.dto.request.InstructorUpdateRequest;
import com.guzem.uzaktan.dto.response.InstructorResponse;
import com.guzem.uzaktan.mapper.instructor.InstructorMapper;
import com.guzem.uzaktan.model.instructor.Instructor;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.instructor.InstructorRepository;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.service.common.FileStorageService;
import com.guzem.uzaktan.service.instructor.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepository;
    private final InstructorMapper instructorMapper;
    private final FileStorageService fileStorageService;
    private final CourseRepository courseRepository;

    @Override
    @Cacheable(value = "instructors")
    public List<InstructorResponse> findAll() {
        return instructorRepository.findAllByOrderByNameAsc()
                .stream()
                .map(instructor -> {
                    long courseCount = courseRepository.countActiveCoursesByInstructorName(instructor.getName());
                    long studentCount = courseRepository.countDistinctStudentsByInstructorName(instructor.getName());
                    return InstructorResponse.builder()
                            .id(instructor.getId())
                            .name(instructor.getName())
                            .expertise(instructor.getExpertise())
                            .photoUrl(instructor.getPhotoUrl())
                            .createdAt(instructor.getCreatedAt())
                            .courseCount(courseCount)
                            .studentCount(studentCount)
                            .build();
                })
                .toList();
    }

    @Override
    @Cacheable(value = "instructor", key = "#id")
    public InstructorResponse findById(Long id) {
        return instructorMapper.toResponse(loadInstructor(id));
    }

    @Override
    public Optional<InstructorResponse> findByName(String name) {
        return instructorRepository.findByName(name)
                .map(instructorMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "instructors", allEntries = true)
    public InstructorResponse create(InstructorCreateRequest request) {
        Instructor instructor = new Instructor();
        instructor.setName(request.getName());
        instructor.setExpertise(request.getExpertise());
        handlePhotoUpload(instructor, request.getPhoto());

        return instructorMapper.toResponse(instructorRepository.save(instructor));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "instructor", key = "#id"),
        @CacheEvict(value = "instructors", allEntries = true)
    })
    public InstructorResponse update(Long id, InstructorUpdateRequest request) {
        Instructor instructor = loadInstructor(id);
        instructor.setName(request.getName());
        instructor.setExpertise(request.getExpertise());
        handlePhotoUpload(instructor, request.getPhoto());

        return instructorMapper.toResponse(instructorRepository.save(instructor));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "instructor", key = "#id"),
        @CacheEvict(value = "instructors", allEntries = true)
    })
    public void delete(Long id) {
        Instructor instructor = loadInstructor(id);
        instructorRepository.delete(instructor);
    }

    private Instructor loadInstructor(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eğitmen", "id", id));
    }

    private void handlePhotoUpload(Instructor instructor, MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            try {
                String photoPath = fileStorageService.storeImage(photo);
                instructor.setPhotoUrl("/uploads/" + photoPath);
            } catch (IOException e) {
                throw new RuntimeException("Resim yüklenirken hata oluştu: " + e.getMessage());
            }
        }
    }
}
