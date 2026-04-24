package com.guzem.uzaktan.model.admin;

import com.guzem.uzaktan.model.course.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "assignment",
       indexes = {
               @Index(name = "idx_assignment_due_date", columnList = "due_date"),
               @Index(name = "idx_assignment_course_id", columnList = "course_id")
       })
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;


    @Nationalized
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Nationalized
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "due_soon_notified_at")
    private LocalDateTime dueSoonNotifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AssignmentSubmission> submissions = new HashSet<>();
}
