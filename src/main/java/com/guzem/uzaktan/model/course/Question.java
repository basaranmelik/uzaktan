package com.guzem.uzaktan.model.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "question",
       indexes = {
               @Index(name = "idx_question_course_id", columnList = "course_id")
       })
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Nationalized
    @Column(name = "question_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    @Nationalized
    @Column(name = "option_a", nullable = false, length = 500)
    private String optionA;

    @Nationalized
    @Column(name = "option_b", nullable = false, length = 500)
    private String optionB;

    @Nationalized
    @Column(name = "option_c", nullable = false, length = 500)
    private String optionC;

    @Nationalized
    @Column(name = "option_d", nullable = false, length = 500)
    private String optionD;

    @Nationalized
    @Column(name = "option_e", nullable = false, length = 500)
    private String optionE;

    @Enumerated(EnumType.STRING)
    @Column(name = "correct_option", nullable = false, length = 1)
    private CorrectOption correctOption;

    @Nationalized
    @Column(name = "explanation", columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
