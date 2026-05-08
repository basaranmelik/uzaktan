package com.guzem.uzaktan.model.common;

import com.guzem.uzaktan.model.admin.AssignmentSubmission;
import com.guzem.uzaktan.model.course.Certificate;
import com.guzem.uzaktan.model.course.CourseReview;
import com.guzem.uzaktan.model.course.Enrollment;
import com.guzem.uzaktan.model.course.QuizAttempt;
import com.guzem.uzaktan.model.course.VideoWatch;
import com.guzem.uzaktan.model.user.CartItem;
import com.guzem.uzaktan.model.user.Notification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"certificates", "enrollments", "submissions", "cartItems", "notifications", "quizAttempts", "reviews", "videoWatches"})
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "email", unique = true, nullable = false, length = 254)
    private String email;

    @Column(name = "password",  nullable = false)
    private String password;

    @Nationalized
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Nationalized
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Embedded
    private Address address;

    @Nationalized
    @Column(name = "bio", columnDefinition = "NVARCHAR(MAX)")
    private String bio;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "zoom_email", length = 254)
    private String zoomEmail;

    @Nationalized
    @Column(name = "skills", columnDefinition = "NVARCHAR(MAX)")
    private String skills;

    @Builder.Default
    @Column(name = "is_password_reset_required", nullable = false)
    private boolean isPasswordResetRequired = false;

    // Spring security
    @Builder.Default
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Builder.Default
    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false, columnDefinition = "int DEFAULT 0")
    private int failedLoginAttempts = 0;

    @Column(name = "lock_until")
    private java.time.LocalDateTime lockUntil;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // İlişkiler
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Certificate> certificates = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AssignmentSubmission> submissions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CartItem> cartItems = new HashSet<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<QuizAttempt> quizAttempts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CourseReview> reviews = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VideoWatch> videoWatches = new HashSet<>();
}
