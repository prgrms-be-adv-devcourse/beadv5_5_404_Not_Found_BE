package com.notfound.product.adapter.in.web;

import com.notfound.product.adapter.in.web.dto.*;
import com.notfound.product.application.port.in.GetProductUseCase;
import com.notfound.product.application.port.in.RegisterProductUseCase;
import com.notfound.product.domain.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final RegisterProductUseCase registerProductUseCase;
    private final GetProductUseCase getProductUseCase;

    public ProductController(RegisterProductUseCase registerProductUseCase,
                             GetProductUseCase getProductUseCase) {
        this.registerProductUseCase = registerProductUseCase;
        this.getProductUseCase = getProductUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductRegisterResponse>> registerProduct(
            @RequestBody @Valid ProductRegisterRequest request) {
        Product product = registerProductUseCase.registerProduct(request.toCommand());
        ProductErrorCode code = ProductErrorCode.PRODUCT_REGISTER_SUCCESS;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(),
                        ProductRegisterResponse.from(product)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable UUID productId) {
        Product product = getProductUseCase.getProduct(productId);
        ProductErrorCode code = ProductErrorCode.PRODUCT_GET_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(),
                ProductDetailResponse.from(product)));
    }
}
