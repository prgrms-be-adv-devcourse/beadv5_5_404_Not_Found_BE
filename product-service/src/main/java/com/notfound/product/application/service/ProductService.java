package com.notfound.product.application.service;

import com.notfound.product.application.port.in.*;
import com.notfound.product.application.port.out.CategoryRepository;
import com.notfound.product.application.port.out.ProductRepository;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.ProductNotFoundException;
import com.notfound.product.domain.model.Product;
import com.notfound.product.domain.model.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService implements
        RegisterProductUseCase,
        GetProductUseCase,
        GetProductListUseCase,
        DeductStockUseCase,
        RestoreStockUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Override
    public Product registerProduct(RegisterProductCommand command) {
        categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));

        Product product = Product.of(
                UUID.randomUUID(),
                command.sellerId(),
                command.categoryId(),
                command.isbn(),
                command.title(),
                command.author(),
                command.publisher(),
                command.price(),
                command.quantity(),
                command.bookType(),
                ProductStatus.PENDING_REVIEW,
                BigDecimal.ZERO,
                0,
                LocalDateTime.now()
        );

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Product getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Product> getProducts(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findAllByIds(ids);
    }

    @Transactional
    @Override
    public void deductStock(DeductStockCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.deductStock(command.quantity());
        productRepository.save(product);
    }

    @Transactional
    @Override
    public void restoreStock(RestoreStockCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.restoreStock(command.quantity());
        productRepository.save(product);
    }
}
