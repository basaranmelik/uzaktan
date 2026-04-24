package com.guzem.uzaktan.model.course;

import com.guzem.uzaktan.model.common.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_watches",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "video_id"}),
       indexes = {
               @Index(name = "idx_videowatch_user_id", columnList = "user_id"),
               @Index(name = "idx_videowatch_video_id", columnList = "video_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private CourseVideo video;

    @CreationTimestamp
    @Column(name = "watched_at", updatable = false)
    private LocalDateTime watchedAt;

    @Column(name = "completed", nullable = false, columnDefinition = "BIT DEFAULT 0")
    @Builder.Default
    private boolean completed = false;

    @Column(name = "watch_time_seconds", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int watchTimeSeconds = 0;

    @Column(name = "max_position_seconds", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int maxPositionSeconds = 0;

    @Column(name = "seek_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int seekCount = 0;

    @Column(name = "authentic", nullable = false, columnDefinition = "BIT DEFAULT 0")
    @Builder.Default
    private boolean authentic = false;
}
