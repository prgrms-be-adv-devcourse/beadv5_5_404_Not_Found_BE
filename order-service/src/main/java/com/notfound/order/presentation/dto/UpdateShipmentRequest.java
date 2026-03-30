package com.notfound.order.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateShipmentRequest(
        @NotBlank(message = "택배사명은 필수입니다.")
        String carrier,

        @NotBlank(message = "송장 번호는 필수입니다.")
        String trackingNumber,

        String shipmentStatus
) {
}
