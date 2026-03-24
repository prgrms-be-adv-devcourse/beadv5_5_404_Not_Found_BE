package com.notfound.product.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.product.adapter.in.web.dto.ProductRegisterRequest;
import com.notfound.product.application.port.in.GetProductUseCase;
import com.notfound.product.application.port.in.RegisterProductUseCase;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RegisterProductUseCase registerProductUseCase;

    @Mock
    private GetProductUseCase getProductUseCase;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(registerProductUseCase, getProductUseCase))
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
    @DisplayName("POST /products - 상품 등록")
    class RegisterProduct {

        @Test
        @DisplayName("유효한 요청이면 201과 등록된 상품을 반환한다")
        void success() throws Exception {
            UUID productId = UUID.randomUUID();
            ProductRegisterRequest request = new ProductRegisterRequest(
                    UUID.randomUUID(), UUID.randomUUID(),
                    "9791234567890", "테스트 도서", "저자", "출판사",
                    15000, 100, BookType.NEW
            );
            given(registerProductUseCase.registerProduct(any())).willReturn(createProduct(productId));

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("PRODUCT_REGISTER_SUCCESS"))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()));
        }

        @Test
        @DisplayName("title이 빈 값이면 400과 INVALID_INPUT_VALUE를 반환한다")
        void fail_whenTitleIsBlank() throws Exception {
            ProductRegisterRequest request = new ProductRegisterRequest(
                    UUID.randomUUID(), UUID.randomUUID(),
                    "9791234567890", "", "저자", "출판사",
                    15000, 100, BookType.NEW
            );

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                    .andExpect(jsonPath("$.data.title").exists());
        }

        @Test
        @DisplayName("카테고리가 존재하지 않으면 404와 CATEGORY_NOT_FOUND를 반환한다")
        void fail_whenCategoryNotFound() throws Exception {
            ProductRegisterRequest request = new ProductRegisterRequest(
                    UUID.randomUUID(), UUID.randomUUID(),
                    "9791234567890", "테스트 도서", "저자", "출판사",
                    15000, 100, BookType.NEW
            );
            given(registerProductUseCase.registerProduct(any()))
                    .willThrow(new CategoryNotFoundException(UUID.randomUUID()));

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /products/{productId} - 상품 상세 조회")
    class GetProduct {

        @Test
        @DisplayName("존재하는 상품이면 200과 상품 상세를 반환한다")
        void success() throws Exception {
            UUID productId = UUID.randomUUID();
            given(getProductUseCase.getProduct(productId)).willReturn(createProduct(productId));

            mockMvc.perform(get("/products/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("PRODUCT_GET_SUCCESS"))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                    .andExpect(jsonPath("$.data.title").value("테스트 도서"));
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
}
