package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.model.course.CourseCategory;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl implements CourseCategoryService {

    private final CourseCategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseCategory> findAll() {
        return categoryRepository.findAllByOrderByDisplayNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseCategory findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseCategory findByDisplayName(String displayName) {
        return categoryRepository.findByDisplayNameIgnoreCase(displayName)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori", "displayName", displayName));
    }

    @Override
    @Transactional
    public CourseCategory create(String displayName) {
        if (categoryRepository.existsByDisplayNameIgnoreCase(displayName.trim())) {
            throw new IllegalArgumentException("Bu isimde bir kategori zaten mevcut.");
        }
        return categoryRepository.save(CourseCategory.builder()
                .displayName(displayName.trim())
                .build());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(findById(id));
    }
}
