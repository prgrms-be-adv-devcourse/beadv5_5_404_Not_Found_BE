package com.notfound.order.application.port.in;

import java.util.List;
import java.util.UUID;

public interface RequestReturnUseCase {
    ReturnResult requestReturn(UUID memberId, UUID orderId, String reason, List<UUID> orderItemIds);

    record ReturnResult(UUID orderId, String returnStatus, List<UUID> orderItemIds) {}
}
