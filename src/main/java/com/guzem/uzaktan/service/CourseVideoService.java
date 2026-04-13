package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.response.CourseVideoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CourseVideoService {

    CourseVideoResponse upload(Long courseId, String title, String description, Integer orderIndex, MultipartFile file) throws IOException;

    List<CourseVideoResponse> uploadMultiple(Long courseId, MultipartFile[] files, String[] titles, Integer[] orderIndices) throws IOException;

    List<CourseVideoResponse> findByCourse(Long courseId);

    List<CourseVideoResponse> findByCourseForStudent(Long courseId, Long userId);

    CourseVideoResponse findById(Long videoId);

    void delete(Long videoId);

    void markWatched(Long videoId, Long userId);

    void updateOrder(Long courseId, List<Long> orderedIds);

    void update(Long videoId, String title, String description);
}
