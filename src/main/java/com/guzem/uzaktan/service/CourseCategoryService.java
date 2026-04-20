package com.guzem.uzaktan.service;

import com.guzem.uzaktan.model.CourseCategory;

import java.util.List;

public interface CourseCategoryService {

    List<CourseCategory> findAll();

    CourseCategory findById(Long id);

    CourseCategory findByDisplayName(String displayName);

    CourseCategory create(String displayName);

    void delete(Long id);
}
