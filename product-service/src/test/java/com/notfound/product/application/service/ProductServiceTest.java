package com.notfound.product.application.service;

import com.notfound.product.application.port.in.*;
import com.notfound.product.application.port.out.CategoryRepository;
import com.notfound.product.application.port.out.ProductRepository;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.InsufficientStockException;
import com.notfound.product.domain.exception.ProductNotFoundException;
import com.notfound.product.domain.model.BookType;
import com.notfound.product.domain.model.Category;
import com.notfound.product.domain.model.Product;
import com.notfound.product.domain.model.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private UUID categoryId;
    private UUID sellerId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        sellerId = UUID.randomUUID();
    }

    private Product createProduct(UUID id, int quantity, ProductStatus status) {
        return Product.of(
                id, sellerId, categoryId,
                "9791234567890", "테스트 도서", "저자", "출판사",
                15000, quantity, BookType.NEW, status,
                BigDecimal.ZERO, 0, LocalDateTime.now()
        );
    }

    private Category createCategory(UUID id) {
        return Category.of(id, null, "국내도서", "domestic", 0, 1, true);
    }

    @Nested
    @DisplayName("상품 등록")
    class RegisterProduct {

        @Test
        @DisplayName("카테고리가 존재하면 PENDING_REVIEW 상태로 상품이 등록된다")
        void success_registerProduct() {
            RegisterProductCommand command = new RegisterProductCommand(
                    sellerId, categoryId, "9791234567890",
                    "테스트 도서", "저자", "출판사", 15000, 100, BookType.NEW
            );
            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.of(createCategory(categoryId)));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            Product result = productService.registerProduct(command);

            assertThat(result.getStatus()).isEqualTo(ProductStatus.PENDING_REVIEW);
            assertThat(result.getTitle()).isEqualTo("테스트 도서");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 CategoryNotFoundException이 발생한다")
        void fail_whenCategoryNotFound() {
            RegisterProductCommand command = new RegisterProductCommand(
                    sellerId, categoryId, "9791234567890",
                    "테스트 도서", "저자", "출판사", 15000, 100, BookType.NEW
            );
            given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.registerProduct(command))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 조회")
    class GetProduct {

        @Test
        @DisplayName("존재하는 상품을 조회할 수 있다")
        void success_getProduct() {
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            Product result = productService.getProduct(productId);

            assertThat(result.getId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 ProductNotFoundException이 발생한다")
        void fail_whenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProduct(productId))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProductList {

        @Test
        @DisplayName("ids가 null이면 전체 상품을 반환한다")
        void success_getAllProducts() {
            List<Product> products = List.of(
                    createProduct(UUID.randomUUID(), 10, ProductStatus.ACTIVE),
                    createProduct(UUID.randomUUID(), 5, ProductStatus.ACTIVE)
            );
            given(productRepository.findAll()).willReturn(products);

            List<Product> result = productService.getProducts(null);

            assertThat(result).hasSize(2);
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("ids가 비어 있으면 전체 상품을 반환한다")
        void success_getAllProducts_whenIdsEmpty() {
            List<Product> products = List.of(createProduct(UUID.randomUUID(), 10, ProductStatus.ACTIVE));
            given(productRepository.findAll()).willReturn(products);

            List<Product> result = productService.getProducts(List.of());

            assertThat(result).hasSize(1);
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("ids가 있으면 해당 상품만 반환한다")
        void success_getProductsByIds() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<UUID> ids = List.of(id1, id2);
            List<Product> products = List.of(
                    createProduct(id1, 10, ProductStatus.ACTIVE),
                    createProduct(id2, 3, ProductStatus.ACTIVE)
            );
            given(productRepository.findAllByIds(ids)).willReturn(products);

            List<Product> result = productService.getProducts(ids);

            assertThat(result).hasSize(2);
            verify(productRepository).findAllByIds(ids);
        }
    }

    @Nested
    @DisplayName("재고 검증")
    class ValidateStock {

        @Test
        @DisplayName("재고가 충분하면 예외가 발생하지 않는다")
        void success_validateStock() {
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> productService.validateStock(new ValidateStockCommand(productId, 5))
            );
        }

        @Test
        @DisplayName("재고가 부족하면 InsufficientStockException이 발생한다")
        void fail_whenStockInsufficient() {
            Product product = createProduct(productId, 3, ProductStatus.ACTIVE);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.validateStock(new ValidateStockCommand(productId, 5)))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("재고 차감")
    class DeductStock {

        @Test
        @DisplayName("재고 차감 후 저장된다")
        void success_deductStock() {
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            productService.deductStock(new DeductStockCommand(productId, 3));

            assertThat(product.getQuantity()).isEqualTo(7);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("존재하지 않는 상품 차감 시 ProductNotFoundException이 발생한다")
        void fail_whenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deductStock(new DeductStockCommand(productId, 3)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("재고 복구")
    class RestoreStock {

        @Test
        @DisplayName("재고 복구 후 저장된다")
        void success_restoreStock() {
            Product product = createProduct(productId, 0, ProductStatus.SOLD_OUT);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            productService.restoreStock(new RestoreStockCommand(productId, 5));

            assertThat(product.getQuantity()).isEqualTo(5);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("존재하지 않는 상품 복구 시 ProductNotFoundException이 발생한다")
        void fail_whenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.restoreStock(new RestoreStockCommand(productId, 5)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }
}
