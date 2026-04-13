package com.guzem.uzaktan.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user", "course"})
@Builder
@Entity
@Table(name = "certificate",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_certificate_user_course",
                columnNames = {"user_id", "course_id"}
        ))
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_code", nullable = false, unique = true, length = 50)
    private String certificateCode;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @CreationTimestamp
    @Column(name = "issue_date", updatable = false)
    private LocalDateTime issueDate;

    // İlişkiler

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
