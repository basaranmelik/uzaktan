package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.model.course.CourseCategory;
import org.springframework.data.domain.Page;
import java.util.List;

public interface CourseQueryService {

    Page<CourseSummaryResponse> findPublishedCourses(String sort, int page, int size);

    Page<CourseSummaryResponse> findByCategory(CourseCategory category, String sort, int page, int size);

    Page<CourseSummaryResponse> search(String keyword, String sort, int page, int size);

    Page<CourseSummaryResponse> findPublishedCoursesForUser(Long userId, String sort, int page, int size);

    CourseResponse findById(Long id);

    Page<CourseResponse> findAllForAdmin(int page, int size);

    List<CourseSummaryResponse> findFeaturedCourses();
}
