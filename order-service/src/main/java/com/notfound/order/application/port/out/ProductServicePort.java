package com.notfound.order.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductServicePort {
    /**
     * Returns product info: productId, productName, price, stock, sellerId, imageUrl
     */
    List<Map<String, Object>> getProducts(List<UUID> productIds);
}
