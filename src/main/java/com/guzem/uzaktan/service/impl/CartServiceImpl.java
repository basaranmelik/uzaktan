package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.response.CartItemResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.model.CartItem;
import com.guzem.uzaktan.model.Course;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.repository.CartItemRepository;
import com.guzem.uzaktan.repository.CourseRepository;
import com.guzem.uzaktan.repository.EnrollmentRepository;
import com.guzem.uzaktan.repository.UserRepository;
import com.guzem.uzaktan.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public void addToCart(Long userId, Long courseId) {
        if (cartItemRepository.existsByUserIdAndCourseId(userId, courseId)) {
            return; // zaten sepette
        }
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalStateException("Bu kursa zaten kayıtlısınız.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        cartItemRepository.save(CartItem.builder().user(user).course(course).build());
    }

    @Override
    public void removeFromCart(Long userId, Long courseId) {
        cartItemRepository.deleteByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(Long userId) {
        return cartItemRepository.findByUserIdWithCourse(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInCart(Long userId, Long courseId) {
        return cartItemRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public BigDecimal getCartTotal(List<CartItemResponse> items) {
        return items.stream()
                .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotalByUser(Long userId) {
        return getCartTotal(getCartItems(userId));
    }

    private CartItemResponse toResponse(CartItem item) {
        Course c = item.getCourse();
        return CartItemResponse.builder()
                .id(item.getId())
                .courseId(c.getId())
                .courseTitle(c.getTitle())
                .categoryDisplayName(c.getCategory() != null ? c.getCategory().getDisplayName() : null)
                .price(c.getPrice())
                .addedAt(item.getAddedAt())
                .build();
    }
}
