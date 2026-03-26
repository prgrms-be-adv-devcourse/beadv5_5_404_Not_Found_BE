package com.notfound.product.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.product.adapter.in.web.dto.ProductRegisterRequest;
import com.notfound.product.adapter.in.web.dto.ProductStatusChangeRequest;
import com.notfound.product.adapter.in.web.dto.ProductUpdateRequest;
import com.notfound.product.application.port.in.*;
import com.notfound.product.application.port.out.SellerStatusVerifier;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.ProductNotFoundException;
import com.notfound.product.domain.model.BookType;
import com.notfound.product.domain.model.Product;
import com.notfound.product.domain.model.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private RegisterProductUseCase registerProductUseCase;
    @Mock private GetProductUseCase getProductUseCase;
    @Mock private GetProductListUseCase getProductListUseCase;
    @Mock private UpdateProductUseCase updateProductUseCase;
    @Mock private ChangeProductStatusUseCase changeProductStatusUseCase;
    @Mock private SellerStatusVerifier sellerStatusVerifier;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(
                        registerProductUseCase, getProductUseCase, getProductListUseCase,
                        updateProductUseCase, changeProductStatusUseCase, sellerStatusVerifier))
                .setCustomArgumentResolvers(new HeaderAuthArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private Product createProduct(UUID id) {
        return Product.of(
                id, UUID.randomUUID(), UUID.randomUUID(),
                "9791234567890", "테스트 도서", "저자", "출판사",
                15000, 100, BookType.NEW,
                ProductStatus.PENDING_REVIEW, BigDecimal.ZERO, 0, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("GET /products - 상품 목록 조회")
    class GetProductList {

        @Test
        @DisplayName("인증 없이 요청해도 200과 전체 상품 목록을 반환한다")
        void success_allProducts() throws Exception {
            List<Product> products = List.of(createProduct(UUID.randomUUID()), createProduct(UUID.randomUUID()));
            given(getProductListUseCase.getProducts(null)).willReturn(products);

            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("PRODUCT_LIST_GET_SUCCESS"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("ids 파라미터로 요청하면 200과 해당 상품 목록을 반환한다")
        void success_byIds() throws Exception {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<Product> products = List.of(createProduct(id1), createProduct(id2));
            given(getProductListUseCase.getProducts(List.of(id1, id2))).willReturn(products);

            mockMvc.perform(get("/products")
                            .param("ids", id1.toString(), id2.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("PRODUCT_LIST_GET_SUCCESS"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /products/{productId} - 상품 상세 조회")
    class GetProduct {

        @Test
        @DisplayName("인증 없이 요청해도 200과 상품 상세를 반환한다")
        void success() throws Exception {
            UUID productId = UUID.randomUUID();
            given(getProductUseCase.getProduct(productId)).willReturn(createProduct(productId));

            mockMvc.perform(get("/products/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("PRODUCT_GET_SUCCESS"))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404와 PRODUCT_NOT_FOUND를 반환한다")
        void fail_whenProductNotFound() throws Exception {
            UUID productId = UUID.randomUUID();
            given(getProductUseCase.getProduct(productId))
                    .willThrow(new ProductNotFoundException(productId));

            mockMvc.perform(get("/products/{productId}", productId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("POST /products - 상품 등록")
    class RegisterProduct {

        private ProductRegisterRequest validRequest() {
            return new ProductRegisterRequest(
                    UUID.randomUUID(),
                    "9791234567890", "테스트 도서", "저자", "출판사",
                    15000, 100, BookType.NEW
            );
        }

        @Test
        @DisplayName("승인된 판매자가 요청하면 201과 등록된 상품을 반환한다")
        void success() throws Exception {
            UUID productId = UUID.randomUUID();
            given(sellerStatusVerifier.isApprovedSeller(any())).willReturn(true);
            given(registerProductUseCase.registerProduct(any())).willReturn(createProduct(productId));

            mockMvc.perform(post("/products")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("PRODUCT_REGISTER_SUCCESS"))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()));
        }

        @Test
        @DisplayName("인증 헤더가 없으면 403과 FORBIDDEN을 반환한다")
        void fail_whenNoAuth() throws Exception {
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("SELLER가 아니면 403과 FORBIDDEN을 반환한다")
        void fail_whenNotSeller() throws Exception {
            mockMvc.perform(post("/products")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "USER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("이메일 미인증 판매자면 403과 EMAIL_NOT_VERIFIED를 반환한다")
        void fail_whenEmailNotVerified() throws Exception {
            mockMvc.perform(post("/products")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "false")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
        }

        @Test
        @DisplayName("미승인 판매자면 403과 SELLER_NOT_APPROVED를 반환한다")
        void fail_whenSellerNotApproved() throws Exception {
            given(sellerStatusVerifier.isApprovedSeller(any())).willReturn(false);

            mockMvc.perform(post("/products")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("SELLER_NOT_APPROVED"));
        }

        @Test
        @DisplayName("title이 빈 값이면 400과 INVALID_INPUT_VALUE를 반환한다")
        void fail_whenTitleIsBlank() throws Exception {
            ProductRegisterRequest request = new ProductRegisterRequest(
                    UUID.randomUUID(),
                    "9791234567890", "", "저자", "출판사",
                    15000, 100, BookType.NEW
            );

            mockMvc.perform(post("/products")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                    .andExpect(jsonPath("$.data.title").exists());
        }

        @Test
        @DisplayName("카테고리가 존재하지 않으면 404와 CATEGORY_NOT_FOUND를 반환한다")
        void fail_whenCategoryNotFound() throws Exception {
            given(sellerStatusVerifier.isApprovedSeller(any())).willReturn(true);
            given(registerProductUseCase.registerProduct(any()))
                    .willThrow(new CategoryNotFoundException(UUID.randomUUID()));

            mockMvc.perform(post("/products")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PATCH /products/{productId} - 상품 수정")
    class UpdateProduct {

        @Test
        @DisplayName("승인된 판매자가 요청하면 200과 수정된 상품을 반환한다")
        void success() throws Exception {
            UUID productId = UUID.randomUUID();
            given(sellerStatusVerifier.isApprovedSeller(any())).willReturn(true);
            given(updateProductUseCase.updateProduct(any())).willReturn(createProduct(productId));

            mockMvc.perform(patch("/products/{productId}", productId)
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ProductUpdateRequest(null, "수정된 제목", null, null, 20000, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("PRODUCT_UPDATE_SUCCESS"));
        }

        @Test
        @DisplayName("SELLER가 아니면 403과 FORBIDDEN을 반환한다")
        void fail_whenNotSeller() throws Exception {
            mockMvc.perform(patch("/products/{productId}", UUID.randomUUID())
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "USER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ProductUpdateRequest(null, "제목", null, null, null, null))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404와 PRODUCT_NOT_FOUND를 반환한다")
        void fail_whenProductNotFound() throws Exception {
            given(sellerStatusVerifier.isApprovedSeller(any())).willReturn(true);
            given(updateProductUseCase.updateProduct(any()))
                    .willThrow(new ProductNotFoundException(UUID.randomUUID()));

            mockMvc.perform(patch("/products/{productId}", UUID.randomUUID())
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ProductUpdateRequest(null, "제목", null, null, null, null))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PATCH /products/{productId}/status - 상품 상태 변경")
    class ChangeProductStatus {

        @Test
        @DisplayName("ADMIN이 요청하면 200과 상태가 변경된 상품을 반환한다")
        void success() throws Exception {
            UUID productId = UUID.randomUUID();
            given(changeProductStatusUseCase.changeProductStatus(any())).willReturn(createProduct(productId));

            mockMvc.perform(patch("/products/{productId}/status", productId)
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "ADMIN")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ProductStatusChangeRequest(ProductStatus.INACTIVE))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("PRODUCT_STATUS_CHANGE_SUCCESS"));
        }

        @Test
        @DisplayName("ADMIN이 아니면 403과 FORBIDDEN을 반환한다")
        void fail_whenNotAdmin() throws Exception {
            mockMvc.perform(patch("/products/{productId}/status", UUID.randomUUID())
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ProductStatusChangeRequest(ProductStatus.INACTIVE))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("status가 null이면 400과 INVALID_INPUT_VALUE를 반환한다")
        void fail_whenStatusIsNull() throws Exception {
            mockMvc.perform(patch("/products/{productId}/status", UUID.randomUUID())
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "ADMIN")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": null}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404와 PRODUCT_NOT_FOUND를 반환한다")
        void fail_whenProductNotFound() throws Exception {
            given(changeProductStatusUseCase.changeProductStatus(any()))
                    .willThrow(new ProductNotFoundException(UUID.randomUUID()));

            mockMvc.perform(patch("/products/{productId}/status", UUID.randomUUID())
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "ADMIN")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ProductStatusChangeRequest(ProductStatus.INACTIVE))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }
    }
}
