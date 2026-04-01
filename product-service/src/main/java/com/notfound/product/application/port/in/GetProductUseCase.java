package com.notfound.product.application.port.in;

import com.notfound.product.domain.model.Product;

import java.util.UUID;

public interface GetProductUseCase {

    Product getProduct(UUID productId);
}
