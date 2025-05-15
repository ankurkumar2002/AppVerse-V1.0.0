
package com.appverse.order_service.mapper;

import com.appverse.order_service.dto.OrderItemResponse;
import com.appverse.order_service.dto.OrderResponse;
import com.appverse.order_service.model.CustomerOrder;
import com.appverse.order_service.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(source = "items", target = "items")
    OrderResponse toOrderResponse(CustomerOrder order);

    List<OrderResponse> toOrderResponseList(List<CustomerOrder> orders);

    // @Mapping(target = "customerOrder", ignore = true) // Avoid circular mapping issues if OrderItem has CustomerOrder
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);

}