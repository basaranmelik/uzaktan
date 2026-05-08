package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CartItemResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String categoryDisplayName;
    private BigDecimal price;
}
