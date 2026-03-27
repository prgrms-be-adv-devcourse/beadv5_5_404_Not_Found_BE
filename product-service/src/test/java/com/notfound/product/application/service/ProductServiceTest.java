package com.notfound.product.application.service;

import com.notfound.product.application.port.in.*;
import com.notfound.product.application.port.out.CategoryRepository;
import com.notfound.product.application.port.out.ProcessedEventRepository;
import com.notfound.product.application.port.out.ProductRepository;
import com.notfound.product.application.port.out.SellerStatusVerifier;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.ForbiddenException;
import com.notfound.product.domain.exception.IsbnDuplicateException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SellerStatusVerifier sellerStatusVerifier;

    @Mock
    private ProcessedEventRepository processedEventRepository;

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
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(true);
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
        @DisplayName("미승인 판매자면 ForbiddenException이 발생한다")
        void fail_whenSellerNotApproved() {
            RegisterProductCommand command = new RegisterProductCommand(
                    sellerId, categoryId, "9791234567890",
                    "테스트 도서", "저자", "출판사", 15000, 100, BookType.NEW
            );
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(false);

            assertThatThrownBy(() -> productService.registerProduct(command))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("ISBN이 중복이면 IsbnDuplicateException이 발생한다")
        void fail_whenIsbnDuplicate() {
            RegisterProductCommand command = new RegisterProductCommand(
                    sellerId, categoryId, "9791234567890",
                    "테스트 도서", "저자", "출판사", 15000, 100, BookType.NEW
            );
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(true);
            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.of(createCategory(categoryId)));
            given(productRepository.existsByIsbn("9791234567890")).willReturn(true);

            assertThatThrownBy(() -> productService.registerProduct(command))
                    .isInstanceOf(IsbnDuplicateException.class);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 CategoryNotFoundException이 발생한다")
        void fail_whenCategoryNotFound() {
            RegisterProductCommand command = new RegisterProductCommand(
                    sellerId, categoryId, "9791234567890",
                    "테스트 도서", "저자", "출판사", 15000, 100, BookType.NEW
            );
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(true);
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
    @DisplayName("재고 차감")
    class DeductStock {

        @Test
        @DisplayName("재고 차감 후 저장된다")
        void success_deductStock() {
            String eventId = "event-1";
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE);
            given(processedEventRepository.existsById(eventId)).willReturn(false);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            productService.deductStock(new DeductStockCommand(eventId,
                    List.of(new DeductStockCommand.StockItem(productId, 3))));

            assertThat(product.getQuantity()).isEqualTo(7);
            verify(processedEventRepository).save(eventId);
        }

        @Test
        @DisplayName("이미 처리된 eventId면 재고 차감을 수행하지 않는다")
        void skip_whenAlreadyProcessed() {
            String eventId = "event-1";
            given(processedEventRepository.existsById(eventId)).willReturn(true);

            productService.deductStock(new DeductStockCommand(eventId,
                    List.of(new DeductStockCommand.StockItem(productId, 3))));

            verify(productRepository, never()).findById(any());
        }

        @Test
        @DisplayName("존재하지 않는 상품 차감 시 ProductNotFoundException이 발생한다")
        void fail_whenProductNotFound() {
            String eventId = "event-1";
            given(processedEventRepository.existsById(eventId)).willReturn(false);
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deductStock(new DeductStockCommand(eventId,
                    List.of(new DeductStockCommand.StockItem(productId, 3)))))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("재고 복구")
    class RestoreStock {

        @Test
        @DisplayName("재고 복구 후 저장된다")
        void success_restoreStock() {
            String eventId = "event-2";
            Product product = createProduct(productId, 0, ProductStatus.SOLD_OUT);
            given(processedEventRepository.existsById(eventId)).willReturn(false);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            productService.restoreStock(new RestoreStockCommand(eventId,
                    List.of(new RestoreStockCommand.StockItem(productId, 5))));

            assertThat(product.getQuantity()).isEqualTo(5);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            verify(processedEventRepository).save(eventId);
        }

        @Test
        @DisplayName("이미 처리된 eventId면 재고 복구를 수행하지 않는다")
        void skip_whenAlreadyProcessed() {
            String eventId = "event-2";
            given(processedEventRepository.existsById(eventId)).willReturn(true);

            productService.restoreStock(new RestoreStockCommand(eventId,
                    List.of(new RestoreStockCommand.StockItem(productId, 5))));

            verify(productRepository, never()).findById(any());
        }

        @Test
        @DisplayName("존재하지 않는 상품 복구 시 ProductNotFoundException이 발생한다")
        void fail_whenProductNotFound() {
            String eventId = "event-2";
            given(processedEventRepository.existsById(eventId)).willReturn(false);
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.restoreStock(new RestoreStockCommand(eventId,
                    List.of(new RestoreStockCommand.StockItem(productId, 5)))))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class UpdateProduct {

        @Test
        @DisplayName("null이 아닌 필드만 수정된다")
        void success_updateProduct() {
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE);
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(true);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            productService.updateProduct(new UpdateProductCommand(
                    sellerId, productId, null, "수정된 제목", null, null, 20000, null));

            assertThat(product.getTitle()).isEqualTo("수정된 제목");
            assertThat(product.getPrice()).isEqualTo(20000);
            assertThat(product.getAuthor()).isEqualTo("저자");
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("미승인 판매자면 ForbiddenException이 발생한다")
        void fail_whenSellerNotApproved() {
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(false);

            assertThatThrownBy(() -> productService.updateProduct(
                    new UpdateProductCommand(sellerId, productId, null, "제목", null, null, null, null)))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("categoryId 수정 시 카테고리 존재를 확인한다")
        void success_updateCategoryId() {
            UUID newCategoryId = UUID.randomUUID();
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE);
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(true);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(categoryRepository.findById(newCategoryId))
                    .willReturn(Optional.of(createCategory(newCategoryId)));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            productService.updateProduct(new UpdateProductCommand(
                    sellerId, productId, newCategoryId, null, null, null, null, null));

            assertThat(product.getCategoryId()).isEqualTo(newCategoryId);
        }

        @Test
        @DisplayName("다른 판매자의 상품 수정 시 ForbiddenException이 발생한다")
        void fail_whenNotOwner() {
            UUID anotherSellerId = UUID.randomUUID();
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE); // sellerId 소유
            given(sellerStatusVerifier.isApprovedSeller(anotherSellerId)).willReturn(true);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.updateProduct(
                    new UpdateProductCommand(anotherSellerId, productId, null, "제목", null, null, null, null)))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("존재하지 않는 상품 수정 시 ProductNotFoundException이 발생한다")
        void fail_whenProductNotFound() {
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(true);
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(
                    new UpdateProductCommand(sellerId, productId, null, "제목", null, null, null, null)))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 수정 시 CategoryNotFoundException이 발생한다")
        void fail_whenCategoryNotFound() {
            UUID newCategoryId = UUID.randomUUID();
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE);
            given(sellerStatusVerifier.isApprovedSeller(sellerId)).willReturn(true);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(categoryRepository.findById(newCategoryId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(
                    new UpdateProductCommand(sellerId, productId, newCategoryId, null, null, null, null, null)))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 상태 변경")
    class ChangeProductStatus {

        @Test
        @DisplayName("상태가 변경되고 저장된다")
        void success_changeStatus() {
            Product product = createProduct(productId, 10, ProductStatus.ACTIVE);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            productService.changeProductStatus(new ChangeProductStatusCommand(productId, ProductStatus.INACTIVE));

            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("존재하지 않는 상품 상태 변경 시 ProductNotFoundException이 발생한다")
        void fail_whenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.changeProductStatus(
                    new ChangeProductStatusCommand(productId, ProductStatus.INACTIVE)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }
}
