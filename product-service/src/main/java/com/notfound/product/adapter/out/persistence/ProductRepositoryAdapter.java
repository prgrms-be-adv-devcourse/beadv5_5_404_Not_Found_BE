package com.notfound.product.adapter.out.persistence;

import com.notfound.product.application.port.out.ProductRepository;
import com.notfound.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = productJpaRepository.findById(product.getId())
                .map(existing -> {
                    existing.updateFrom(product);
                    return existing;
                })
                .orElseGet(() -> ProductJpaEntity.from(product));
        return productJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId)
                .map(ProductJpaEntity::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll().stream()
                .map(ProductJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAllByIds(List<UUID> ids) {
        return productJpaRepository.findAllByIdIn(ids).stream()
                .map(ProductJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return productJpaRepository.existsByIsbn(isbn);
    }
}
