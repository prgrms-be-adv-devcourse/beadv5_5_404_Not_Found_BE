package com.notfound.order.presentation.dto;

import com.notfound.order.application.port.in.command.CreateOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotEmpty(message = "주문할 상품이 없습니다.")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "배송지를 선택해주세요.")
        UUID addressId
) {
    public record OrderItemRequest(
            @NotNull(message = "상품 ID는 필수입니다.")
            UUID productId,

            @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
            int quantity,

            UUID cartItemId
    ) {}

    public CreateOrderCommand toCommand() {
        var commandItems = items.stream()
                .map(i -> new CreateOrderCommand.OrderItemCommand(i.productId(), i.quantity(), i.cartItemId()))
                .toList();
        return new CreateOrderCommand(commandItems, addressId);
    }
}
