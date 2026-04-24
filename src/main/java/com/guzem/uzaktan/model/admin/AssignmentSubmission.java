package com.guzem.uzaktan.model.admin;

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
@Table(name = "assignment_submission",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_submission_assignment_user",
                columnNames = {"assignment_id", "user_id"}
        ))
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(name = "text_answer", columnDefinition = "NVARCHAR(MAX)")
    private String textAnswer;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Nationalized
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "score")
    private Integer score;

    @Nationalized
    @Column(name = "feedback", columnDefinition = "NVARCHAR(MAX)")
    private String feedback;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
