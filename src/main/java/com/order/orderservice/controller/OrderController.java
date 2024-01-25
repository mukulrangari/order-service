package com.order.orderservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.order.orderservice.Dto.OrderRequestDto;
import com.order.orderservice.Dto.OrderResponseDto;
import com.order.orderservice.entity.PurchaseOrder;
import com.order.orderservice.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/order")
    public OrderResponseDto createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        log.info("OrderController: Create order request for userId:{}, productId:{}", orderRequestDto.getUserId(),
                orderRequestDto.getProductId());
        return orderService.createOrder(orderRequestDto);
    }

    @PostMapping("/order/cancel/{orderId}")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable String orderId) {
        log.info("OrderController: Cancelling order request for orderId:{}", orderId);
        return orderService.cancelOrderRequest(orderId);
    }

    @GetMapping("/orders")
    public List<PurchaseOrder> getOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/order")
    public List<PurchaseOrder> getOrdersByUserId(@RequestParam Integer userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/order/{id}")
    public PurchaseOrder getOrderByOrderId(@PathVariable String id) {
        return orderService.getOrderByOrderId(id).orElse(null);
    }
}
