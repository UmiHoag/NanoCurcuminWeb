package com.nanoCurcuminWeb.service.order;

import com.nanoCurcuminWeb.dto.OrderDto;
import com.nanoCurcuminWeb.model.Order;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrder(Long orderId);
    List<OrderDto> getUserOrders(Long userId);

    OrderDto convertToDto(Order order);
}
