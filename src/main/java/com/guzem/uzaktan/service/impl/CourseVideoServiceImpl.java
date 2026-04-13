package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.response.CourseVideoResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.model.Course;
import com.guzem.uzaktan.model.CourseVideo;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.model.VideoWatch;
import com.guzem.uzaktan.mapper.CourseVideoMapper;
import com.guzem.uzaktan.repository.CourseRepository;
import com.guzem.uzaktan.repository.CourseVideoRepository;
import com.guzem.uzaktan.repository.UserRepository;
import com.guzem.uzaktan.repository.VideoWatchRepository;
import com.guzem.uzaktan.service.CourseVideoService;
import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseVideoServiceImpl implements CourseVideoService {

    private final CourseVideoRepository courseVideoRepository;
    private final VideoWatchRepository videoWatchRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final EnrollmentService enrollmentService;
    private final CourseVideoMapper courseVideoMapper;

    @Override
    public CourseVideoResponse upload(Long courseId, String title, String description,
                                      Integer orderIndex, MultipartFile file) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        int moduleCount = course.getModule() != null ? course.getModule() : 0;
        long existingCount = courseVideoRepository.countByCourseId(courseId);
        if (moduleCount > 0 && existingCount >= moduleCount) {
            throw new IllegalArgumentException(
                    "Bu kursa en fazla " + moduleCount + " video eklenebilir. Mevcut: " + existingCount + ".");
        }

        String filePath = fileStorageService.storeVideo(file, courseId);
        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "video";

        if (orderIndex == null) {
            orderIndex = (int) (courseVideoRepository.countByCourseId(courseId) + 1);
        }

        CourseVideo video = CourseVideo.builder()
                .course(course)
                .title(title)
                .description(description)
                .orderIndex(orderIndex)
                .filePath(filePath)
                .originalFileName(originalFileName)
                .build();

        CourseVideo saved = courseVideoRepository.save(video);

        return courseVideoMapper.toResponse(saved, false);
    }

    @Override
    public List<CourseVideoResponse> uploadMultiple(Long courseId, MultipartFile[] files,
                                                    String[] titles, Integer[] orderIndices) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        int moduleCount = course.getModule() != null ? course.getModule() : 0;
        List<CourseVideo> existing = courseVideoRepository.findByCourseIdOrderByOrderIndex(courseId);
        int existingCount = existing.size();

        long nonEmptyCount = java.util.Arrays.stream(files).filter(f -> !f.isEmpty()).count();
        if (moduleCount > 0 && (existingCount + nonEmptyCount) > moduleCount) {
            throw new IllegalArgumentException(
                    "Bu kursa en fazla " + moduleCount + " video eklenebilir. " +
                    "Mevcut: " + existingCount + ", eklenmek istenen: " + nonEmptyCount + ".");
        }

        // Build pending list with desired positions
        record Pending(MultipartFile file, String title, int desiredPos) {}
        List<Pending> pending = new java.util.ArrayList<>();
        int fileIdx = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isEmpty()) continue;
            String original = files[i].getOriginalFilename() != null ? files[i].getOriginalFilename() : "video";
            String videoTitle = (titles != null && i < titles.length && titles[i] != null && !titles[i].trim().isEmpty())
                    ? titles[i].trim()
                    : (original.contains(".") ? original.substring(0, original.lastIndexOf('.')) : original);
            int desiredPos = (orderIndices != null && i < orderIndices.length && orderIndices[i] != null)
                    ? Math.max(1, orderIndices[i])
                    : existingCount + fileIdx + 1;
            pending.add(new Pending(files[i], videoTitle, desiredPos));
            fileIdx++;
        }

        // Process in ascending desired-position order so earlier insertions shift correctly
        pending.sort(java.util.Comparator.comparingInt(Pending::desiredPos));

        // Working list starts with existing videos
        List<CourseVideo> workingList = new java.util.ArrayList<>(existing);

        List<CourseVideoResponse> results = new java.util.ArrayList<>();
        int insertionOffset = 0;
        for (Pending p : pending) {
            String filePath = fileStorageService.storeVideo(p.file(), courseId);
            String original = p.file().getOriginalFilename() != null ? p.file().getOriginalFilename() : "video";
            CourseVideo video = CourseVideo.builder()
                    .course(course)
                    .title(p.title())
                    .orderIndex(0)
                    .filePath(filePath)
                    .originalFileName(original)
                    .build();
            int insertAt = Math.min(Math.max(0, p.desiredPos() + insertionOffset - 1), workingList.size());
            workingList.add(insertAt, video);
            insertionOffset++;
        }

        // Renumber all and save new ones (existing managed entities auto-flush)
        for (int i = 0; i < workingList.size(); i++) {
            workingList.get(i).setOrderIndex(i + 1);
        }
        for (CourseVideo v : workingList) {
            if (v.getId() == null) {
                results.add(courseVideoMapper.toResponse(courseVideoRepository.save(v), false));
            }
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseVideoResponse> findByCourse(Long courseId) {
        return courseVideoRepository.findByCourseIdOrderByOrderIndex(courseId)
                .stream()
                .map(v -> courseVideoMapper.toResponse(v, false))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseVideoResponse> findByCourseForStudent(Long courseId, Long userId) {
        Set<Long> watchedIds = videoWatchRepository.findWatchedVideoIdsByUserAndCourse(userId, courseId);
        return courseVideoRepository.findByCourseIdOrderByOrderIndex(courseId)
                .stream()
                .map(v -> courseVideoMapper.toResponse(v, watchedIds.contains(v.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseVideoResponse findById(Long videoId) {
        CourseVideo video = loadVideo(videoId);
        return courseVideoMapper.toResponse(video, false);
    }

    @Override
    public void delete(Long videoId) {
        CourseVideo video = loadVideo(videoId);

        // Önce bu videoya ait tüm izleme kayıtlarını sil (FK kısıtlaması)
        videoWatchRepository.deleteByVideoId(videoId);

        fileStorageService.delete(video.getFilePath());
        courseVideoRepository.delete(video);
    }

    @Override
    public void markWatched(Long videoId, Long userId) {
        if (videoWatchRepository.existsByUserIdAndVideoId(userId, videoId)) {
            return; // already watched
        }

        CourseVideo video = loadVideo(videoId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));

        VideoWatch watch = VideoWatch.builder()
                .user(user)
                .video(video)
                .build();
        videoWatchRepository.save(watch);

        enrollmentService.recalculateProgress(userId, video.getCourse().getId());
    }

    @Override
    public void updateOrder(Long courseId, List<Long> orderedIds) {
        for (int i = 0; i < orderedIds.size(); i++) {
            CourseVideo v = loadVideo(orderedIds.get(i));
            v.setOrderIndex(i + 1);
        }
    }

    @Override
    public void update(Long videoId, String title, String description) {
        CourseVideo v = loadVideo(videoId);
        v.setTitle(title.trim());
        v.setDescription(description != null && !description.isBlank() ? description.trim() : null);
    }

    private CourseVideo loadVideo(Long id) {
        return courseVideoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", id));
    }

}
