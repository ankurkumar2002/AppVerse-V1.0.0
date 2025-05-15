// === In Order Service Project ===
package com.appverse.order_service.service;

import com.appverse.order_service.dto.CreateOrderRequest;
import com.appverse.order_service.dto.OrderResponse;
import com.appverse.order_service.dto.PaymentUpdateDto;
import java.util.List;
// No UUID import needed if orderId is String

public interface OrderService {

    OrderResponse createOrder(String userId, CreateOrderRequest request);

    OrderResponse getOrderById(String orderId); // Changed from UUID to String

    List<OrderResponse> getOrdersByUserId(String userId);

    OrderResponse processPaymentUpdate(PaymentUpdateDto paymentUpdateDto); // paymentUpdateDto.orderId is now String

    OrderResponse cancelOrder(String orderId, String userId); // Changed from UUID to String
}