package com.order.orderservice.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.order.orderservice.Dto.OrderEvent;
import com.order.orderservice.Dto.OrderStatus;
import com.order.orderservice.service.OrderService;

@Service
public class EventHandler {

    @Autowired
    private OrderService orderService;

    public void eventProcceser(OrderEvent orderEvent) {
        switch (orderEvent.getOrderStatus()) {
            case OrderStatus.PAYMENT_COMPLETED:
                orderService.processOrder(orderEvent);
                break;
            case OrderStatus.PAYMENT_FAILED:
                orderService.cancelOrder(orderEvent);
                break;
            case OrderStatus.PAYMENT_REFUNDED:
                orderService.cancelOrder(orderEvent);
                break;
            case OrderStatus.ITEM_OUT_OF_STOCK:
                orderService.cancelOrder(orderEvent);
                break;
            case OrderStatus.SHIPMENT_CREATED:
                orderService.shippingOrder(orderEvent);
                break;
            case OrderStatus.SHIPMENT_COMPLETED:
                orderService.completeOrder(orderEvent);
                break;
            case OrderStatus.SHIPMENT_FAILED:
                orderService.updateShipmentStatus(orderEvent);
                break;
            default:
                break;
        }
    }

}
