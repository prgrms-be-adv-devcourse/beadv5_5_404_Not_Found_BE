package com.notfound.product.adapter.in.web.dto;

import com.notfound.product.domain.model.BookType;
import com.notfound.product.domain.model.Product;
import com.notfound.product.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDetailResponse(
        UUID productId,
        UUID sellerId,
        UUID categoryId,
        String isbn,
        String title,
        String author,
        String publisher,
        int price,
        int quantity,
        BookType bookType,
        ProductStatus status,
        BigDecimal avgRating,
        int reviewCount,
        LocalDateTime createdAt
) {
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getSellerId(),
                product.getCategoryId(),
                product.getIsbn(),
                product.getTitle(),
                product.getAuthor(),
                product.getPublisher(),
                product.getPrice(),
                product.getQuantity(),
                product.getBookType(),
                product.getStatus(),
                product.getAvgRating(),
                product.getReviewCount(),
                product.getCreatedAt()
        );
    }
}
