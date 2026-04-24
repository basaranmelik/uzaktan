package com.guzem.uzaktan.repository.user;

import com.guzem.uzaktan.model.user.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.course WHERE ci.user.id = :userId ORDER BY ci.addedAt DESC")
    List<CartItem> findByUserIdWithCourse(@Param("userId") Long userId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    int countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId AND ci.course.id = :courseId")
    void deleteByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
