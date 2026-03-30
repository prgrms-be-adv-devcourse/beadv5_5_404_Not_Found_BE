package com.notfound.product.application.port.in;

import com.notfound.product.domain.model.Product;

public interface RegisterProductUseCase {

    Product registerProduct(RegisterProductCommand command);
}
