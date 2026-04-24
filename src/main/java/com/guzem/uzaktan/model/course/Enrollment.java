package com.guzem.uzaktan.model.course;

import com.guzem.uzaktan.model.common.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_enrollment_user_course",
                columnNames = {"user_id", "course_id"}
        ),
        indexes = {
                @Index(name = "idx_enrollment_user_id", columnList = "user_id"),
                @Index(name = "idx_enrollment_course_id", columnList = "course_id"),
                @Index(name = "idx_enrollment_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Builder.Default
    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EnrollmentStatus status;

    @CreationTimestamp
    @Column(name = "enrollment_date", updatable = false)
    private LocalDateTime enrollmentDate;

    // İlişkiler

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}