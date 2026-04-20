package com.guzem.uzaktan.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"videos", "enrollments", "assignments", "certificates", "reviews", "instructor", "zoomMeetings"})
@Builder
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Nationalized
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Nationalized
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "quota")
    private Integer quota;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "hours")
    private Integer hours;

    @Column(name = "module")
    private Integer module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CourseCategory category;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "course_type", nullable = false, length = 30)
    private CourseType courseType = CourseType.ONLINE;

    @Nationalized
    @Column(name = "location", length = 300)
    private String location;

    @Nationalized
    @Column(name = "course_schedule", length = 300)
    private String courseSchedule;

    @Nationalized
    @Column(name = "schedule_days", length = 100)
    private String scheduleDays;

    @Column(name = "schedule_start_time", length = 8)
    private String scheduleStartTime;

    @Column(name = "schedule_end_time", length = 8)
    private String scheduleEndTime;

    @Nationalized
    @Column(name = "manual_curriculum", columnDefinition = "NVARCHAR(MAX)")
    private String manualCurriculum;

    @Column(name = "certificate_deadline")
    private LocalDate certificateDeadline;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private CourseStatus status = CourseStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 20)
    private CourseLevel level;

    @Builder.Default
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Builder.Default
    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Nationalized
    @Column(name = "instructor_name", length = 150)
    private String instructorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CourseVideo> videos = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Assignment> assignments = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Certificate> certificates = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CourseReview> reviews = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ZoomMeeting> zoomMeetings = new HashSet<>();
}
