package com.notfound.product.domain.model;

import com.notfound.product.domain.exception.InsufficientStockException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final UUID sellerId;
    private final UUID categoryId;
    private final String isbn;
    private final String title;
    private final String author;
    private final String publisher;
    private final int price;
    private int quantity;
    private final BookType bookType;
    private final ProductStatus status;
    private final BigDecimal avgRating;
    private final int reviewCount;
    private final LocalDateTime createdAt;

    private Product(UUID id, UUID sellerId, UUID categoryId, String isbn, String title,
                    String author, String publisher, int price, int quantity,
                    BookType bookType, ProductStatus status, BigDecimal avgRating,
                    int reviewCount, LocalDateTime createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.quantity = quantity;
        this.bookType = bookType;
        this.status = status;
        this.avgRating = avgRating;
        this.reviewCount = reviewCount;
        this.createdAt = createdAt;
    }

    public static Product of(UUID id, UUID sellerId, UUID categoryId, String isbn, String title,
                             String author, String publisher, int price, int quantity,
                             BookType bookType, ProductStatus status, BigDecimal avgRating,
                             int reviewCount, LocalDateTime createdAt) {
        return new Product(id, sellerId, categoryId, isbn, title, author, publisher,
                price, quantity, bookType, status, avgRating, reviewCount, createdAt);
    }

    public void validateStock(int requestedQuantity) {
        if (this.quantity < requestedQuantity) {
            throw new InsufficientStockException(this.id, requestedQuantity, this.quantity);
        }
    }

    public void deductStock(int quantity) {
        validateStock(quantity);
        this.quantity -= quantity;
    }

    public void restoreStock(int quantity) {
        this.quantity += quantity;
    }

    public boolean isAvailable() {
        return this.status == ProductStatus.AVAILABLE && this.quantity > 0;
    }

    public UUID getId() { return id; }
    public UUID getSellerId() { return sellerId; }
    public UUID getCategoryId() { return categoryId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public BookType getBookType() { return bookType; }
    public ProductStatus getStatus() { return status; }
    public BigDecimal getAvgRating() { return avgRating; }
    public int getReviewCount() { return reviewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
