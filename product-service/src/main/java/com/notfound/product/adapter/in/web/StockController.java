package com.notfound.product.adapter.in.web;

import com.notfound.product.adapter.in.web.dto.*;
import com.notfound.product.application.port.in.*;
import com.notfound.product.domain.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/stock")
public class StockController {

    private final GetProductUseCase getProductUseCase;
    private final ValidateStockUseCase validateStockUseCase;
    private final DeductStockUseCase deductStockUseCase;
    private final RestoreStockUseCase restoreStockUseCase;

    public StockController(GetProductUseCase getProductUseCase,
                           ValidateStockUseCase validateStockUseCase,
                           DeductStockUseCase deductStockUseCase,
                           RestoreStockUseCase restoreStockUseCase) {
        this.getProductUseCase = getProductUseCase;
        this.validateStockUseCase = validateStockUseCase;
        this.deductStockUseCase = deductStockUseCase;
        this.restoreStockUseCase = restoreStockUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<StockResponse>> getStock(@PathVariable UUID productId) {
        Product product = getProductUseCase.getProduct(productId);
        ProductErrorCode code = ProductErrorCode.STOCK_GET_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(),
                StockResponse.from(product)));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Void>> validateStock(
            @PathVariable UUID productId,
            @RequestBody @Valid StockRequest request) {
        validateStockUseCase.validateStock(new ValidateStockCommand(productId, request.quantity()));
        ProductErrorCode code = ProductErrorCode.STOCK_VALIDATE_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(), null));
    }

    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<Void>> deductStock(
            @PathVariable UUID productId,
            @RequestBody @Valid StockRequest request) {
        deductStockUseCase.deductStock(new DeductStockCommand(productId, request.quantity()));
        ProductErrorCode code = ProductErrorCode.STOCK_DEDUCT_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(), null));
    }

    @PostMapping("/restore")
    public ResponseEntity<ApiResponse<Void>> restoreStock(
            @PathVariable UUID productId,
            @RequestBody @Valid StockRequest request) {
        restoreStockUseCase.restoreStock(new RestoreStockCommand(productId, request.quantity()));
        ProductErrorCode code = ProductErrorCode.STOCK_RESTORE_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(), null));
    }
}
