package com.notfound.product.application.port.in;

import com.notfound.product.domain.model.Product;

public interface ChangeProductStatusUseCase {

    Product changeProductStatus(ChangeProductStatusCommand command);
}
