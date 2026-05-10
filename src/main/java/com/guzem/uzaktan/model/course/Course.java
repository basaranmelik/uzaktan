package com.guzem.uzaktan.model.course;

import com.guzem.uzaktan.model.admin.Assignment;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.model.user.CartItem;
import java.util.ArrayList;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"videos", "enrollments", "assignments", "certificates", "reviews", "instructor", "instructors", "zoomMeetings", "questions", "cartItems", "quizAttempts"})
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

    // ── UZEM Form Alanları ──────────────────────────────────────────────────

    @Nationalized
    @Column(name = "aim", columnDefinition = "NVARCHAR(MAX)")
    private String aim;

    @Column(name = "min_hours")
    private Integer minHours;

    @Column(name = "max_hours")
    private Integer maxHours;

    @Nationalized
    @Column(name = "course_version", length = 20)
    private String courseVersion;

    @Nationalized
    @Column(name = "prepared_by", length = 150)
    private String preparedBy;

    @Column(name = "prepared_date")
    private LocalDate preparedDate;

    @Nationalized
    @Column(name = "reviewed_by", length = 150)
    private String reviewedBy;

    @Nationalized
    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Nationalized
    @Column(name = "training_method", columnDefinition = "NVARCHAR(MAX)")
    private String trainingMethod;

    @Nationalized
    @Column(name = "used_materials", columnDefinition = "NVARCHAR(MAX)")
    private String usedMaterials;

    @Nationalized
    @Column(name = "used_platform", length = 300)
    private String usedPlatform;

    @Nationalized
    @Column(name = "instructor_notes", columnDefinition = "NVARCHAR(MAX)")
    private String instructorNotes;

    /** JSON: List&lt;String&gt; — Hedef Kitle */
    @Nationalized
    @Column(name = "target_audience", columnDefinition = "NVARCHAR(MAX)")
    private String targetAudience;

    /** JSON: List&lt;String&gt; — Eğitim İçeriği / Konu Başlıkları */
    @Nationalized
    @Column(name = "content_topics", columnDefinition = "NVARCHAR(MAX)")
    private String contentTopics;

    /** JSON: List&lt;String&gt; — Eğitim Kazanımları */
    @Nationalized
    @Column(name = "learning_outcomes", columnDefinition = "NVARCHAR(MAX)")
    private String learningOutcomes;

    /** JSON: List&lt;String&gt; — Ön Koşullar */
    @Nationalized
    @Column(name = "prerequisites", columnDefinition = "NVARCHAR(MAX)")
    private String prerequisites;

    /** JSON: List&lt;AssessmentItem&gt; — Ölçme ve Değerlendirme */
    @Nationalized
    @Column(name = "assessment_items", columnDefinition = "NVARCHAR(MAX)")
    private String assessmentItems;

    @Builder.Default
    @Column(name = "featured", nullable = false)
    private boolean featured = false;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_instructors",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> instructors = new ArrayList<>();

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

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Question> questions = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CartItem> cartItems = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<QuizAttempt> quizAttempts = new HashSet<>();
}
