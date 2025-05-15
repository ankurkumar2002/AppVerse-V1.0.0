package com.appverse.cart_service.mapper;

import com.appverse.cart_service.dto.CartItemResponse;
import com.appverse.cart_service.dto.CartResponse;
import com.appverse.cart_service.model.Cart;
import com.appverse.cart_service.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    // If Cart.id is UUID, this is fine. If it's String, adjust.
    @Mapping(source = "id", target = "cartId")
    CartResponse toCartResponse(Cart cart);
    List<CartResponse> toCartResponseList(List<Cart> carts);

    CartItemResponse toCartItemResponse(CartItem cartItem);
    List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems);
}