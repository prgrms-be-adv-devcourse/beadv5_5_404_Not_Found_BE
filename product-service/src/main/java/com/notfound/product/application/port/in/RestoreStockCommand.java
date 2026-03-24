package com.notfound.product.application.port.in;

import java.util.UUID;

public record RestoreStockCommand(
        UUID productId,
        int quantity
) {}
