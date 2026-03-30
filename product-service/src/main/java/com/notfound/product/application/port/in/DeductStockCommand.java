package com.notfound.product.application.port.in;

import java.util.List;
import java.util.UUID;

public record DeductStockCommand(String eventId, List<StockItem> items) {

    public record StockItem(UUID productId, int quantity) {}
}
