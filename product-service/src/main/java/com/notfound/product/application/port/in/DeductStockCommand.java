package com.notfound.product.application.port.in;

import java.util.UUID;

public record DeductStockCommand(
        UUID productId,
        int quantity
) {}
