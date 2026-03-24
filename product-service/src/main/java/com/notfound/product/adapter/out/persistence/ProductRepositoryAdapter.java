package com.notfound.product.adapter.out.persistence;

import com.notfound.product.application.port.out.ProductRepository;
import com.notfound.product.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = ProductJpaEntity.from(product);
        return productJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId)
                .map(ProductJpaEntity::toDomain);
    }
}
