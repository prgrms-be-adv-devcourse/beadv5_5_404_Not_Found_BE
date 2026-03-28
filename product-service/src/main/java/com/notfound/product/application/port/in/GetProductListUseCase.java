package com.notfound.product.application.port.in;

import com.notfound.product.domain.model.Product;

import java.util.List;
import java.util.UUID;

public interface GetProductListUseCase {

    List<Product> getProducts(List<UUID> ids);
}
