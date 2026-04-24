package com.guzem.uzaktan.model.instructor;

import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.common.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "zoom_meeting")
public class ZoomMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "zoom_meeting_id", nullable = false, length = 50)
    private String zoomMeetingId;

    @Nationalized
    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    @Column(name = "join_url", nullable = false, length = 500)
    private String joinUrl;

    @Column(name = "start_url", nullable = false, length = 1000)
    private String startUrl;

    @Column(name = "password", length = 20)
    private String password;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private ZoomMeetingStatus status = ZoomMeetingStatus.SCHEDULED;

    @Column(name = "recording_url", length = 500)
    private String recordingUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
