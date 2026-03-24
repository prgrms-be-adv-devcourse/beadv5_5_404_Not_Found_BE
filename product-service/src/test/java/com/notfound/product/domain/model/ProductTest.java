package com.notfound.product.domain.model;

import com.notfound.product.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private Product createProduct(int quantity, ProductStatus status) {
        return Product.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "9791234567890",
                "테스트 도서",
                "저자",
                "출판사",
                15000,
                quantity,
                BookType.PAPER,
                status,
                BigDecimal.ZERO,
                0,
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("재고 검증")
    class ValidateStock {

        @Test
        @DisplayName("재고가 충분하면 예외가 발생하지 않는다")
        void success_whenStockIsSufficient() {
            Product product = createProduct(10, ProductStatus.AVAILABLE);

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> product.validateStock(5));
        }

        @Test
        @DisplayName("요청 수량과 재고가 같으면 예외가 발생하지 않는다")
        void success_whenStockEqualsRequested() {
            Product product = createProduct(5, ProductStatus.AVAILABLE);

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> product.validateStock(5));
        }

        @Test
        @DisplayName("재고가 부족하면 InsufficientStockException이 발생한다")
        void fail_whenStockIsInsufficient() {
            Product product = createProduct(3, ProductStatus.AVAILABLE);

            assertThatThrownBy(() -> product.validateStock(5))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("재고 차감")
    class DeductStock {

        @Test
        @DisplayName("정상 차감 시 재고가 줄어든다")
        void success_quantityDecreased() {
            Product product = createProduct(10, ProductStatus.AVAILABLE);

            product.deductStock(3);

            assertThat(product.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("재고 전량 차감이 가능하다")
        void success_deductAllStock() {
            Product product = createProduct(5, ProductStatus.AVAILABLE);

            product.deductStock(5);

            assertThat(product.getQuantity()).isZero();
        }

        @Test
        @DisplayName("재고보다 많은 수량 차감 시 InsufficientStockException이 발생한다")
        void fail_whenDeductMoreThanStock() {
            Product product = createProduct(3, ProductStatus.AVAILABLE);

            assertThatThrownBy(() -> product.deductStock(5))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("재고 복구")
    class RestoreStock {

        @Test
        @DisplayName("재고 복구 시 수량이 증가한다")
        void success_quantityIncreased() {
            Product product = createProduct(5, ProductStatus.AVAILABLE);

            product.restoreStock(3);

            assertThat(product.getQuantity()).isEqualTo(8);
        }

        @Test
        @DisplayName("재고가 0일 때도 복구가 가능하다")
        void success_restoreFromZero() {
            Product product = createProduct(0, ProductStatus.AVAILABLE);

            product.restoreStock(5);

            assertThat(product.getQuantity()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("구매 가능 여부")
    class IsAvailable {

        @Test
        @DisplayName("AVAILABLE 상태이고 재고가 있으면 구매 가능하다")
        void available_whenStatusAvailableAndStockExists() {
            Product product = createProduct(1, ProductStatus.AVAILABLE);

            assertThat(product.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("AVAILABLE 상태여도 재고가 0이면 구매 불가능하다")
        void notAvailable_whenStockIsZero() {
            Product product = createProduct(0, ProductStatus.AVAILABLE);

            assertThat(product.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("DISCONTINUED 상태이면 재고가 있어도 구매 불가능하다")
        void notAvailable_whenDiscontinued() {
            Product product = createProduct(10, ProductStatus.DISCONTINUED);

            assertThat(product.isAvailable()).isFalse();
        }
    }
}
