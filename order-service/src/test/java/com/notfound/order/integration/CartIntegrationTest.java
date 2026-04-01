package com.notfound.order.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MEMBER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String PRODUCT_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private static final String PRODUCT_ID2 = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    private String cartItemId;
    private String cartItemId2;

    @Test
    @Order(1)
    @DisplayName("장바구니 상품 추가 성공")
    void addCartItem_success() throws Exception {
        String body = """
                {
                    "productId": "%s",
                    "quantity": 2
                }
                """.formatted(PRODUCT_ID);

        MvcResult result = mockMvc.perform(post("/order/cart/item")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CART_ITEM_CREATE_SUCCESS"))
                .andExpect(jsonPath("$.data.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        cartItemId = json.get("data").get("cartItemId").asText();
    }

    @Test
    @Order(2)
    @DisplayName("다른 상품 추가 성공")
    void addCartItem_anotherProduct() throws Exception {
        String body = """
                {
                    "productId": "%s",
                    "quantity": 1
                }
                """.formatted(PRODUCT_ID2);

        MvcResult result = mockMvc.perform(post("/order/cart/item")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CART_ITEM_CREATE_SUCCESS"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        cartItemId2 = json.get("data").get("cartItemId").asText();
    }

    @Test
    @Order(3)
    @DisplayName("동일 상품 추가 시 수량 합산")
    void addCartItem_sameProduct_addsQuantity() throws Exception {
        String body = """
                {
                    "productId": "%s",
                    "quantity": 3
                }
                """.formatted(PRODUCT_ID);

        mockMvc.perform(post("/order/cart/item")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantity").value(5));
    }

    @Test
    @Order(4)
    @DisplayName("장바구니 조회 성공")
    void getCart_success() throws Exception {
        mockMvc.perform(get("/order/cart")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CART_FETCH_SUCCESS"))
                .andExpect(jsonPath("$.data.items.length()").value(2));
    }

    @Test
    @Order(5)
    @DisplayName("장바구니 수량 수정 성공")
    void updateCartItem_success() throws Exception {
        String body = """
                { "quantity": 10 }
                """;

        mockMvc.perform(patch("/order/cart/item/{cartItemId}", cartItemId)
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CART_ITEM_UPDATE_SUCCESS"))
                .andExpect(jsonPath("$.data.quantity").value(10));
    }

    @Test
    @Order(6)
    @DisplayName("장바구니 항목 삭제 성공")
    void deleteCartItem_success() throws Exception {
        mockMvc.perform(delete("/order/cart/item/{cartItemId}", cartItemId2)
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CART_ITEM_DELETE_SUCCESS"));
    }

    @Test
    @Order(7)
    @DisplayName("삭제 후 장바구니 조회 - 1개만 남아야 함")
    void getCart_afterDelete() throws Exception {
        mockMvc.perform(get("/order/cart")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @Order(8)
    @DisplayName("장바구니 비우기 성공")
    void clearCart_success() throws Exception {
        mockMvc.perform(delete("/order/cart")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CART_CLEAR_SUCCESS"));
    }

    @Test
    @Order(9)
    @DisplayName("비우기 후 장바구니 조회 - 빈 장바구니")
    void getCart_afterClear() throws Exception {
        mockMvc.perform(get("/order/cart")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    // ===== 실패 케이스 =====

    @Test
    @Order(10)
    @DisplayName("인증 없이 장바구니 조회 → 401")
    void getCart_unauthorized() throws Exception {
        mockMvc.perform(get("/order/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @Order(11)
    @DisplayName("존재하지 않는 항목 수정 → 404")
    void updateCartItem_notFound() throws Exception {
        mockMvc.perform(patch("/order/cart/item/00000000-0000-0000-0000-000000000000")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "quantity": 5 }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CART_ITEM_NOT_FOUND"));
    }

    @Test
    @Order(12)
    @DisplayName("존재하지 않는 항목 삭제 → 404")
    void deleteCartItem_notFound() throws Exception {
        mockMvc.perform(delete("/order/cart/item/00000000-0000-0000-0000-000000000000")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CART_ITEM_NOT_FOUND"));
    }

    @Test
    @Order(13)
    @DisplayName("장바구니 없는 회원 조회 → 404")
    void getCart_noCart() throws Exception {
        mockMvc.perform(get("/order/cart")
                        .header("X-User-Id", "99999999-9999-9999-9999-999999999999")
                        .header("X-Role", "USER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CART_NOT_FOUND"));
    }
}
