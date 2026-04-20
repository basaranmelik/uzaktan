package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Long> {

    Optional<CourseCategory> findByDisplayNameIgnoreCase(String displayName);

    List<CourseCategory> findAllByOrderByDisplayNameAsc();

    boolean existsByDisplayNameIgnoreCase(String displayName);
}
