import { CartItemResponse } from "./CartItemResponse";

export interface CartResponse {
    cartId: string,
    userId: string,
    items: CartItemResponse[],
    createdAt: string,
    updatedAt: string
}