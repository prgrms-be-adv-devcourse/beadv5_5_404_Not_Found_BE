package com.notfound.product.application.port.out;

import com.notfound.product.domain.model.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID productId);
}
