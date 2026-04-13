package com.guzem.uzaktan.mapper;

import com.guzem.uzaktan.dto.response.CourseVideoResponse;
import com.guzem.uzaktan.model.CourseVideo;
import org.springframework.stereotype.Component;

@Component
public class CourseVideoMapper {

    public CourseVideoResponse toResponse(CourseVideo video, boolean watched) {
        return CourseVideoResponse.builder()
                .id(video.getId())
                .courseId(video.getCourse().getId())
                .courseTitle(video.getCourse().getTitle())
                .title(video.getTitle())
                .description(video.getDescription())
                .orderIndex(video.getOrderIndex())
                .filePath(video.getFilePath())
                .originalFileName(video.getOriginalFileName())
                .createdAt(video.getCreatedAt())
                .watched(watched)
                .build();
    }
}
