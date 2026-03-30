package com.notfound.product.adapter.in.web;

import com.notfound.product.adapter.in.web.dto.ApiResponse;
import com.notfound.product.adapter.in.web.dto.DeductStockRequest;
import com.notfound.product.adapter.in.web.dto.ProductErrorCode;
import com.notfound.product.application.port.in.DeductStockCommand;
import com.notfound.product.application.port.in.DeductStockUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductStockController {

    private final DeductStockUseCase deductStockUseCase;

    @PostMapping("/stock/deduct")
    public ResponseEntity<ApiResponse<Void>> deductStock(
            @RequestBody @Valid DeductStockRequest request) {
        List<DeductStockCommand.StockItem> items = request.items().stream()
                .map(item -> new DeductStockCommand.StockItem(item.productId(), item.quantity()))
                .toList();
        deductStockUseCase.deductStock(new DeductStockCommand(items));
        ProductErrorCode code = ProductErrorCode.STOCK_DEDUCT_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(), null));
    }
}
