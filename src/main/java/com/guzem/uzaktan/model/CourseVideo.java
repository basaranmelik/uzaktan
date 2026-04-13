package com.guzem.uzaktan.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Nationalized
    @Column(nullable = false, length = 200)
    private String title;

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Nationalized
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
