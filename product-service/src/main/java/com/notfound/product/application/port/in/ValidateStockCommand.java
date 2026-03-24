package com.notfound.product.application.port.in;

import java.util.UUID;

public record ValidateStockCommand(
        UUID productId,
        int quantity
) {}
