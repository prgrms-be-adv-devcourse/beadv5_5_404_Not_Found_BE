package com.notfound.product.application.port.in;

import java.util.UUID;

public record CreateCategoryCommand(
        UUID parentId,
        String name,
        String slug,
        int sortOrder
) {}
