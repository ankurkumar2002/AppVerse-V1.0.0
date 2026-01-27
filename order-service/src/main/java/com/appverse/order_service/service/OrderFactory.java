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
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderFactory {

    private final AppServiceClient appServiceClient;

    public CustomerOrder create(String userId, CreateOrderRequest request) {

        log.info("▶️ OrderFactory.create started | userId={} | items={}",
                userId, request.items().size());

        CustomerOrder order = CustomerOrder.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .build();

        for (CreateOrderItemRequest itemReq : request.items()) {

            log.info("➡️ Processing order item | appId={} | qty={} | type={}",
                    itemReq.applicationId(),
                    itemReq.quantity(),
                    itemReq.itemType());

            AppDetails app = fetchApp(itemReq.applicationId());

            log.info("✅ App fetched | id={} | name={} | price={} | free={}",
                    app.id(), app.name(), app.price(), app.isFree());

            OrderItemType itemType = convert(itemReq.itemType());

            BigDecimal unitPrice = app.isFree() ? BigDecimal.ZERO : app.price();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));

            OrderItem item = OrderItem.builder()
                    .id(UUID.randomUUID().toString())
                    .applicationId(app.id())
                    .applicationName(app.name())
                    .unitPrice(unitPrice)
                    .quantity(itemReq.quantity())
                    .totalPrice(totalPrice) 
                    .currency(app.currency())
                    .itemType(convert(itemReq.itemType()))
                    .fulfillmentStatus(FulfillmentStatus.PENDING)
                    .build();

            order.addItem(item);

            log.info("🧾 Item added | appId={} | itemType={} | unitPrice={}",
                    app.id(), itemType, item.getUnitPrice());
        }

        order.calculateTotals();

        log.info("💰 Order totals calculated | orderId={} | totalAmount={}",
                order.getId());

        log.info("✅ OrderFactory.create completed | orderId={}", order.getId());

        return order;
    }

    private AppDetails fetchApp(String id) {
        try {
            log.info("🌐 Calling app-service for appId={}", id);
            return appServiceClient.getAppDetails(id);
        } catch (FeignException e) {
            log.error("❌ Failed to fetch app from app-service | appId={} | status={}",
                    id, e.status(), e);
            throw new ServiceUnavailableException(
                    "Application service unavailable for appId=" + id, e);
        }
    }

    private OrderItemType convert(OrderItemTypeDto dtoType) {
        if (dtoType == null) {
            log.error("❌ OrderItemTypeDto is NULL");
            throw new IllegalArgumentException("OrderItemType cannot be null");
        }

        OrderItemType converted = switch (dtoType) {
            case ONE_TIME_PURCHASE -> OrderItemType.ONE_TIME_PURCHASE;
            case SUBSCRIPTION_INITIAL_PURCHASE -> OrderItemType.SUBSCRIPTION_INITIAL_PURCHASE;
        };

        log.info("🔄 Item type converted | dtoType={} -> domainType={}",
                dtoType, converted);

        return converted;
    }
}
