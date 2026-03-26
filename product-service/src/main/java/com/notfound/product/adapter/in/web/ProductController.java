package com.notfound.product.adapter.in.web;

import com.notfound.product.adapter.in.web.dto.*;
import com.notfound.product.application.port.in.*;
import com.notfound.product.application.port.out.SellerStatusVerifier;
import com.notfound.product.domain.exception.ForbiddenException;
import com.notfound.product.domain.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final RegisterProductUseCase registerProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final GetProductListUseCase getProductListUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final SellerStatusVerifier sellerStatusVerifier;

    public ProductController(RegisterProductUseCase registerProductUseCase,
                             GetProductUseCase getProductUseCase,
                             GetProductListUseCase getProductListUseCase,
                             UpdateProductUseCase updateProductUseCase,
                             ChangeProductStatusUseCase changeProductStatusUseCase,
                             SellerStatusVerifier sellerStatusVerifier) {
        this.registerProductUseCase = registerProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.getProductListUseCase = getProductListUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.changeProductStatusUseCase = changeProductStatusUseCase;
        this.sellerStatusVerifier = sellerStatusVerifier;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductRegisterResponse>> registerProduct(
            @AuthUser AuthenticatedUser user,
            @RequestBody @Valid ProductRegisterRequest request) {
        requireApprovedSeller(user);
        UUID sellerId = UUID.fromString(user.userId());
        Product product = registerProductUseCase.registerProduct(request.toCommand(sellerId));
        ProductErrorCode code = ProductErrorCode.PRODUCT_REGISTER_SUCCESS;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(),
                        ProductRegisterResponse.from(product)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDetailResponse>>> getProducts(
            @RequestParam(required = false) List<UUID> ids) {
        List<Product> products = getProductListUseCase.getProducts(ids);
        ProductErrorCode code = ProductErrorCode.PRODUCT_LIST_GET_SUCCESS;
        List<ProductDetailResponse> response = products.stream()
                .map(ProductDetailResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(), response));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable UUID productId) {
        Product product = getProductUseCase.getProduct(productId);
        ProductErrorCode code = ProductErrorCode.PRODUCT_GET_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(),
                ProductDetailResponse.from(product)));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID productId,
            @RequestBody @Valid ProductUpdateRequest request) {
        requireApprovedSeller(user);
        Product product = updateProductUseCase.updateProduct(request.toCommand(productId));
        ProductErrorCode code = ProductErrorCode.PRODUCT_UPDATE_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(),
                ProductDetailResponse.from(product)));
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> changeProductStatus(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID productId,
            @RequestBody @Valid ProductStatusChangeRequest request) {
        requireAdmin(user);
        Product product = changeProductStatusUseCase.changeProductStatus(request.toCommand(productId));
        ProductErrorCode code = ProductErrorCode.PRODUCT_STATUS_CHANGE_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(),
                ProductDetailResponse.from(product)));
    }

    private void requireApprovedSeller(AuthenticatedUser user) {
        if (user == null || !"SELLER".equals(user.role())) {
            throw new ForbiddenException(ProductErrorCode.FORBIDDEN.getMessage());
        }
        if (!user.emailVerified()) {
            throw new ForbiddenException(ProductErrorCode.EMAIL_NOT_VERIFIED.getMessage());
        }
        if (!sellerStatusVerifier.isApprovedSeller(UUID.fromString(user.userId()))) {
            throw new ForbiddenException(ProductErrorCode.SELLER_NOT_APPROVED.getMessage());
        }
    }

    private void requireAdmin(AuthenticatedUser user) {
        if (user == null || !"ADMIN".equals(user.role())) {
            throw new ForbiddenException(ProductErrorCode.FORBIDDEN.getMessage());
        }
    }
}
