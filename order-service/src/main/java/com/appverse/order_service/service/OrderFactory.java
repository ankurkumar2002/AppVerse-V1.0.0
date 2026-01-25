package com.appverse.order_service.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.appverse.order_service.client.AppServiceClient;
import com.appverse.order_service.dto.AppDetails;
import com.appverse.order_service.dto.CreateOrderItemRequest;
import com.appverse.order_service.dto.CreateOrderRequest;
import com.appverse.order_service.dto.OrderItemTypeDto;
import com.appverse.order_service.enums.FulfillmentStatus;
import com.appverse.order_service.enums.OrderItemType;
import com.appverse.order_service.enums.OrderStatus;
import com.appverse.order_service.exception.ServiceUnavailableException;
import com.appverse.order_service.model.CustomerOrder;
import com.appverse.order_service.model.OrderItem;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderFactory {

    private final AppServiceClient appServiceClient;

    public CustomerOrder create(String userId, CreateOrderRequest request) {

        CustomerOrder order = CustomerOrder.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .build();

        for (CreateOrderItemRequest itemReq : request.items()) {

            AppDetails app = fetchApp(itemReq.applicationId());

            OrderItem item = OrderItem.builder()
                    .id(UUID.randomUUID().toString())
                    .applicationId(app.id())
                    .applicationName(app.name())
                    .unitPrice(app.isFree() ? BigDecimal.ZERO : app.price())
                    .quantity(itemReq.quantity())
                    .currency(app.currency())
                    .itemType(convert(itemReq.itemType()))
                    .fulfillmentStatus(FulfillmentStatus.PENDING)
                    .build();

            order.addItem(item);
        }

        order.calculateTotals();
        return order;
    }

    private AppDetails fetchApp(String id) {
        try {
            return appServiceClient.getAppDetails(id);
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Application service unavailable", e);
        }
    }

    private OrderItemType convert(OrderItemTypeDto dtoType) {
        if (dtoType == null) {
            throw new IllegalArgumentException("OrderItemType cannot be null");
        }
        return switch (dtoType) {
            case ONE_TIME_PURCHASE -> OrderItemType.ONE_TIME_PURCHASE;
            case SUBSCRIPTION_INITIAL_PURCHASE -> OrderItemType.SUBSCRIPTION_INITIAL_PURCHASE;
        };
    }

}
