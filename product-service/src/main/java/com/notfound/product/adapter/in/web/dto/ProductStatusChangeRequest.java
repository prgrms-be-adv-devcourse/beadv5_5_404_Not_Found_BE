package com.notfound.product.adapter.in.web.dto;

import com.notfound.product.application.port.in.ChangeProductStatusCommand;
import com.notfound.product.domain.model.ProductStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductStatusChangeRequest(

        @NotNull
        ProductStatus status
) {
    public ChangeProductStatusCommand toCommand(UUID productId) {
        return new ChangeProductStatusCommand(productId, status);
    }
}
