package com.notfound.product.application.port.in;

import com.notfound.product.domain.model.ProductStatus;

import java.util.UUID;

public record ChangeProductStatusCommand(
        UUID productId,
        ProductStatus status
) {}
