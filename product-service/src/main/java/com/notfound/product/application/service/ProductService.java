package com.notfound.product.application.service;

import com.notfound.product.application.port.in.*;
import com.notfound.product.application.port.out.CategoryRepository;
import com.notfound.product.application.port.out.ProductRepository;
import com.notfound.product.application.port.out.SellerStatusVerifier;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.ForbiddenException;
import com.notfound.product.domain.exception.IsbnDuplicateException;
import com.notfound.product.domain.exception.ProductNotFoundException;
import com.notfound.product.domain.model.Product;
import com.notfound.product.domain.model.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService implements
        RegisterProductUseCase,
        GetProductUseCase,
        GetProductListUseCase,
        UpdateProductUseCase,
        ChangeProductStatusUseCase,
        DeductStockUseCase,
        RestoreStockUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerStatusVerifier sellerStatusVerifier;

    @Transactional
    @Override
    public Product registerProduct(RegisterProductCommand command) {
        if (!sellerStatusVerifier.isApprovedSeller(command.sellerId())) {
            throw new ForbiddenException("승인된 판매자가 아닙니다.");
        }

        categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));

        if (productRepository.existsByIsbn(command.isbn())) {
            throw new IsbnDuplicateException(command.isbn());
        }

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
    public Product updateProduct(UpdateProductCommand command) {
        if (!sellerStatusVerifier.isApprovedSeller(command.sellerId())) {
            throw new ForbiddenException("승인된 판매자가 아닙니다.");
        }

        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        if (!product.getSellerId().equals(command.sellerId())) {
            throw new ForbiddenException("본인의 상품만 수정할 수 있습니다.");
        }

        if (command.categoryId() != null) {
            categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));
        }

        product.update(command.categoryId(), command.title(), command.author(),
                command.publisher(), command.price(), command.quantity());
        return productRepository.save(product);
    }

    @Transactional
    @Override
    public Product changeProductStatus(ChangeProductStatusCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.changeStatus(command.status());
        return productRepository.save(product);
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
