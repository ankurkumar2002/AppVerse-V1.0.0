package com.appverse.order_service.event.payload;

import com.appverse.order_service.enums.OrderItemType;
import java.math.BigDecimal;

public record OrderItemSummaryPayload(
    String orderItemId,
    String applicationId,
    String applicationName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice,
    OrderItemType itemType
) {}