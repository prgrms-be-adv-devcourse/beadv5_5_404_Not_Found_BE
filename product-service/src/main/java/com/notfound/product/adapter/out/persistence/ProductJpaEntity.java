package com.notfound.product.adapter.out.persistence;

import com.notfound.product.domain.model.BookType;
import com.notfound.product.domain.model.Product;
import com.notfound.product.domain.model.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
public class ProductJpaEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 200)
    private String author;

    @Column(nullable = false, length = 100)
    private String publisher;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_type", nullable = false, length = 20)
    private BookType bookType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ProductJpaEntity from(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.id = product.getId();
        entity.sellerId = product.getSellerId();
        entity.categoryId = product.getCategoryId();
        entity.isbn = product.getIsbn();
        entity.title = product.getTitle();
        entity.author = product.getAuthor();
        entity.publisher = product.getPublisher();
        entity.price = product.getPrice();
        entity.quantity = product.getQuantity();
        entity.bookType = product.getBookType();
        entity.status = product.getStatus();
        entity.avgRating = product.getAvgRating();
        entity.reviewCount = product.getReviewCount();
        entity.createdAt = product.getCreatedAt();
        return entity;
    }

    public void updateFrom(Product product) {
        this.categoryId = product.getCategoryId();
        this.title = product.getTitle();
        this.author = product.getAuthor();
        this.publisher = product.getPublisher();
        this.price = product.getPrice();
        this.quantity = product.getQuantity();
        this.status = product.getStatus();
        this.avgRating = product.getAvgRating();
        this.reviewCount = product.getReviewCount();
    }

    public Product toDomain() {
        return Product.of(
                id, sellerId, categoryId, isbn, title, author, publisher,
                price, quantity, bookType, status, avgRating, reviewCount, createdAt
        );
    }
}
