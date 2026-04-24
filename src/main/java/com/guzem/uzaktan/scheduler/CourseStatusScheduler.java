package com.guzem.uzaktan.scheduler;

import com.guzem.uzaktan.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseStatusScheduler {

    private final CourseService courseService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleCourseStatusUpdates() {
        log.info("Starting scheduled job: updateCourseStatuses");
        try {
            courseService.updateCourseStatuses();
            log.info("Successfully updated course statuses");
        } catch (Exception e) {
            log.error("Error occurred while updating course statuses", e);
        }
    }
}
