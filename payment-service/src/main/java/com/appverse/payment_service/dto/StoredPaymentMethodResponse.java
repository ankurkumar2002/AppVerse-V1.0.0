package com.appverse.payment_service.dto;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentMethodType;
import com.appverse.payment_service.enums.StoredPaymentMethodStatus;

import java.time.Instant;
import java.util.Map;

public record StoredPaymentMethodResponse(
    String id,
    String userId,
    PaymentGatewayType paymentGateway,
    String gatewayCustomerId,
    String gatewayPaymentMethodId, // The actual stored ID after processing the token
    PaymentMethodType type,
    String brand,
    String last4,
    Integer expiryMonth,
    Integer expiryYear,
    boolean isDefault,
    Map<String, String> billingDetails,
    StoredPaymentMethodStatus status,
    Instant addedAt,
    Instant updatedAt
) {}