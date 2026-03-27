package com.notfound.product.domain.model;

package com.notfound.product.domain.model;

public enum ProductStatus {
    PENDING_REVIEW,
    ACTIVE,
    INACTIVE,
    SOLD_OUT;

    public boolean canTransitionTo(ProductStatus next) {
        return switch (this) {
            case PENDING_REVIEW -> next == ACTIVE || next == INACTIVE;
            case ACTIVE -> next == INACTIVE;
            case INACTIVE -> next == ACTIVE;
            case SOLD_OUT -> false;
        };
    }
}
