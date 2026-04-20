package com.guzem.uzaktan.config;

import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.repository.CourseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseCategoryConverter implements Converter<String, CourseCategory> {

    private final CourseCategoryRepository categoryRepository;

    @Override
    public CourseCategory convert(String source) {
        if (source == null || source.isBlank()) return null;
        return categoryRepository.findByDisplayNameIgnoreCase(source).orElse(null);
    }
}
