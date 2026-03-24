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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
                BookType.NEW,
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
            Product product = createProduct(10, ProductStatus.ACTIVE);

            assertDoesNotThrow(() -> product.validateStock(5));
        }

        @Test
        @DisplayName("요청 수량과 재고가 같으면 예외가 발생하지 않는다")
        void success_whenStockEqualsRequested() {
            Product product = createProduct(5, ProductStatus.ACTIVE);

            assertDoesNotThrow(() -> product.validateStock(5));
        }

        @Test
        @DisplayName("재고가 부족하면 InsufficientStockException이 발생한다")
        void fail_whenStockIsInsufficient() {
            Product product = createProduct(3, ProductStatus.ACTIVE);

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
            Product product = createProduct(10, ProductStatus.ACTIVE);

            product.deductStock(3);

            assertThat(product.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("재고 전량 차감 시 상태가 SOLD_OUT으로 변경된다")
        void success_statusChangesToSoldOut_whenQuantityBecomesZero() {
            Product product = createProduct(5, ProductStatus.ACTIVE);

            product.deductStock(5);

            assertThat(product.getQuantity()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("재고보다 많은 수량 차감 시 InsufficientStockException이 발생한다")
        void fail_whenDeductMoreThanStock() {
            Product product = createProduct(3, ProductStatus.ACTIVE);

            assertThatThrownBy(() -> product.deductStock(5))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("INACTIVE 상태에서는 차감해도 상태가 변경되지 않는다")
        void statusNotChanged_whenInactive() {
            Product product = createProduct(5, ProductStatus.INACTIVE);

            product.deductStock(5);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("재고 복구")
    class RestoreStock {

        @Test
        @DisplayName("재고 복구 시 수량이 증가한다")
        void success_quantityIncreased() {
            Product product = createProduct(5, ProductStatus.ACTIVE);

            product.restoreStock(3);

            assertThat(product.getQuantity()).isEqualTo(8);
        }

        @Test
        @DisplayName("SOLD_OUT 상태에서 복구 시 상태가 ACTIVE로 변경된다")
        void success_statusChangesToActive_whenRestoredFromSoldOut() {
            Product product = createProduct(0, ProductStatus.SOLD_OUT);

            product.restoreStock(5);

            assertThat(product.getQuantity()).isEqualTo(5);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("INACTIVE 상태에서는 복구해도 상태가 변경되지 않는다")
        void statusNotChanged_whenInactive() {
            Product product = createProduct(0, ProductStatus.INACTIVE);

            product.restoreStock(5);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("구매 가능 여부")
    class IsAvailable {

        @Test
        @DisplayName("ACTIVE 상태이고 재고가 있으면 구매 가능하다")
        void available_whenStatusActiveAndStockExists() {
            Product product = createProduct(1, ProductStatus.ACTIVE);

            assertThat(product.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE 상태여도 재고가 0이면 구매 불가능하다")
        void notAvailable_whenStockIsZero() {
            Product product = createProduct(0, ProductStatus.ACTIVE);

            assertThat(product.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("SOLD_OUT 상태이면 구매 불가능하다")
        void notAvailable_whenSoldOut() {
            Product product = createProduct(0, ProductStatus.SOLD_OUT);

            assertThat(product.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("INACTIVE 상태이면 재고가 있어도 구매 불가능하다")
        void notAvailable_whenInactive() {
            Product product = createProduct(10, ProductStatus.INACTIVE);

            assertThat(product.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("PENDING_REVIEW 상태이면 재고가 있어도 구매 불가능하다")
        void notAvailable_whenPendingReview() {
            Product product = createProduct(10, ProductStatus.PENDING_REVIEW);

            assertThat(product.isAvailable()).isFalse();
        }
    }
}
