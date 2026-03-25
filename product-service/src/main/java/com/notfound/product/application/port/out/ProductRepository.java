package com.notfound.product.application.port.out;

import com.notfound.product.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID productId);

    List<Product> findAll();

    List<Product> findAllByIds(List<UUID> ids);
}
