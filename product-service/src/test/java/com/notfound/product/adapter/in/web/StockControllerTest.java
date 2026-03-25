package com.notfound.product.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.product.adapter.in.web.dto.StockRequest;
import com.notfound.product.application.port.in.*;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private GetProductUseCase getProductUseCase;
    @Mock private DeductStockUseCase deductStockUseCase;
    @Mock private RestoreStockUseCase restoreStockUseCase;

    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new StockController(
                        getProductUseCase, deductStockUseCase, restoreStockUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private Product createProduct(int quantity, ProductStatus status) {
        return Product.of(
                productId, UUID.randomUUID(), UUID.randomUUID(),
                "9791234567890", "테스트 도서", "저자", "출판사",
                15000, quantity, BookType.NEW,
                status, BigDecimal.ZERO, 0, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("GET /products/{productId}/stock - 재고 조회")
    class GetStock {

        @Test
        @DisplayName("존재하는 상품이면 200과 재고 정보를 반환한다")
        void success() throws Exception {
            given(getProductUseCase.getProduct(productId))
                    .willReturn(createProduct(50, ProductStatus.ACTIVE));

            mockMvc.perform(get("/products/{productId}/stock", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("STOCK_GET_SUCCESS"))
                    .andExpect(jsonPath("$.data.quantity").value(50))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404와 PRODUCT_NOT_FOUND를 반환한다")
        void fail_whenProductNotFound() throws Exception {
            given(getProductUseCase.getProduct(productId))
                    .willThrow(new ProductNotFoundException(productId));

            mockMvc.perform(get("/products/{productId}/stock", productId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("POST /products/{productId}/stock/deduct - 재고 차감")
    class DeductStock {

        @Test
        @DisplayName("정상 차감이면 200과 STOCK_DEDUCT_SUCCESS를 반환한다")
        void success() throws Exception {
            mockMvc.perform(post("/products/{productId}/stock/deduct", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StockRequest(3))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("STOCK_DEDUCT_SUCCESS"));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404와 PRODUCT_NOT_FOUND를 반환한다")
        void fail_whenProductNotFound() throws Exception {
            doThrow(new ProductNotFoundException(productId))
                    .when(deductStockUseCase).deductStock(any());

            mockMvc.perform(post("/products/{productId}/stock/deduct", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StockRequest(3))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("POST /products/{productId}/stock/restore - 재고 복구")
    class RestoreStock {

        @Test
        @DisplayName("정상 복구이면 200과 STOCK_RESTORE_SUCCESS를 반환한다")
        void success() throws Exception {
            mockMvc.perform(post("/products/{productId}/stock/restore", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StockRequest(3))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("STOCK_RESTORE_SUCCESS"));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404와 PRODUCT_NOT_FOUND를 반환한다")
        void fail_whenProductNotFound() throws Exception {
            doThrow(new ProductNotFoundException(productId))
                    .when(restoreStockUseCase).restoreStock(any());

            mockMvc.perform(post("/products/{productId}/stock/restore", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StockRequest(3))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }
    }
}
