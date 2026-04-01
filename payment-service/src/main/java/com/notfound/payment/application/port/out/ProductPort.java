package com.notfound.payment.application.port.out;

import java.util.List;
import java.util.UUID;

public interface ProductPort {

    void deductStock(List<StockItem> items);

    record StockItem(
            UUID productId,
            int quantity
    ) {}
}
