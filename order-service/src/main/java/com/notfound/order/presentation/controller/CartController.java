package com.notfound.order.presentation.controller;

import com.notfound.order.application.port.in.*;
import com.notfound.order.domain.model.Cart;
import com.notfound.order.domain.model.CartItem;
import com.notfound.order.infrastructure.security.AuthUser;
import com.notfound.order.infrastructure.security.AuthenticatedUser;
import com.notfound.order.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequestMapping("/order/cart")
public class CartController {

    private final AddCartItemUseCase addCartItemUseCase;
    private final GetCartUseCase getCartUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final DeleteCartItemUseCase deleteCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;

    public CartController(AddCartItemUseCase addCartItemUseCase,
                          GetCartUseCase getCartUseCase,
                          UpdateCartItemUseCase updateCartItemUseCase,
                          DeleteCartItemUseCase deleteCartItemUseCase,
                          ClearCartUseCase clearCartUseCase) {
        this.addCartItemUseCase = addCartItemUseCase;
        this.getCartUseCase = getCartUseCase;
        this.updateCartItemUseCase = updateCartItemUseCase;
        this.deleteCartItemUseCase = deleteCartItemUseCase;
        this.clearCartUseCase = clearCartUseCase;
    }

    @Operation(summary = "장바구니 조회", description = "장바구니 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthUser AuthenticatedUser user) {

        UUID memberId = UUID.fromString(user.userId());
        Cart cart = getCartUseCase.getCart(memberId);
        List<CartItem> items = getCartUseCase.getCartItems(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "CART_FETCH_SUCCESS",
                        "장바구니 조회에 성공했습니다.", CartResponse.from(cart, items)));
    }

    @Operation(summary = "장바구니 상품 추가", description = "장바구니에 상품 추가")
    @PostMapping("/item")
    public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody AddCartItemRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        CartItem item = addCartItemUseCase.addCartItem(memberId, request.productId(), request.quantity());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "CART_ITEM_CREATE_SUCCESS",
                        "장바구니에 상품이 추가되었습니다.", CartItemResponse.from(item)));
    }

    @Operation(summary = "장바구니 수량 수정", description = "장바구니 항목 수량 변경")
    @PatchMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        CartItem item = updateCartItemUseCase.updateCartItemQuantity(memberId, cartItemId, request.quantity());

        return ResponseEntity.ok(
                ApiResponse.success(200, "CART_ITEM_UPDATE_SUCCESS",
                        "장바구니 수량이 수정되었습니다.", CartItemResponse.from(item)));
    }

    @Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 항목 삭제")
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID cartItemId) {

        UUID memberId = UUID.fromString(user.userId());
        deleteCartItemUseCase.deleteCartItem(memberId, cartItemId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "CART_ITEM_DELETE_SUCCESS",
                        "장바구니 항목이 삭제되었습니다.", null));
    }

    @Operation(summary = "장바구니 비우기", description = "장바구니 전체 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthUser AuthenticatedUser user) {

        UUID memberId = UUID.fromString(user.userId());
        clearCartUseCase.clearCart(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "CART_CLEAR_SUCCESS",
                        "장바구니가 비워졌습니다.", null));
    }
}
