package com.order.orderservice.jobs;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.order.orderservice.Dto.OrderEvent;
import com.order.orderservice.Dto.OrderStatus;
import com.order.orderservice.entity.InventoryProduct;
import com.order.orderservice.entity.PaymentWaiting;
import com.order.orderservice.entity.Product;
import com.order.orderservice.entity.PurchaseOrder;
import com.order.orderservice.entity.ShipmentWaiting;
import com.order.orderservice.event.OrderEventPublisher;
import com.order.orderservice.repo.InventroyRepository;
import com.order.orderservice.repo.OrderRepository;
import com.order.orderservice.repo.PaymentWaitingRepo;
import com.order.orderservice.repo.ProductRepository;
import com.order.orderservice.repo.ShipmentWaitingRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderJobs {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ShipmentWaitingRepo shipmentWaitingRepo;
    @Autowired
    private PaymentWaitingRepo paymentWaitingRepo;
    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventroyRepository inventroyRepository;

    // @Scheduled(fixedDelay = 10000)
    public void paymentJobs() throws InterruptedException {
        List<PaymentWaiting> paymentWaitings = paymentWaitingRepo.findAll();
        paymentWaitings.forEach(paymentWaiting -> {
            log.info("payment purchaseOrder ---> {}", paymentWaiting);
            if (Duration.between(paymentWaiting.getCreateTime(), Instant.now()).toSeconds() > 6) {
                PurchaseOrder order = orderRepository.findById(paymentWaiting.getOrderId()).get();
                order.setCurrentOrderStatus(OrderStatus.ORDER_CANCELLED);
                ArrayList<OrderStatus> activity = order.getOrderStatusHistory();
                activity.add(OrderStatus.PAYMENT_TIMED_OUT);
                activity.add(OrderStatus.ORDER_CANCELLED);
                order.setOrderStatusHistory(activity);
                orderRepository.save(order);
                paymentWaitingRepo.delete(paymentWaiting);
            }
        });
        // List<PurchaseOrder> purchaseOrder =
        // orderRepository.findByCurrentOrderStatusAndTime(
        // Instant.now().minusSeconds(10), Instant.now(), OrderStatus.ITEM_IN_STOCK);
        // log.info("payment purchaseOrder ---> {}", purchaseOrder);
        // purchaseOrder.forEach(order -> {
        // order.setCurrentOrderStatus(OrderStatus.ORDER_CANCELLED);
        // log.info("payment time --> " + Duration.between(order.getCreateTime(),
        // Instant.now()).toSeconds());
        // if (Duration.between(order.getCreateTime(), Instant.now()).toSeconds() > 5) {
        // ArrayList<OrderStatus> activity = order.getOrderStatusHistory();
        // activity.add(OrderStatus.PAYMENT_TIMED_OUT);
        // activity.add(OrderStatus.ORDER_CANCELLED);
        // order.setOrderStatusHistory(activity);
        // orderRepository.save(order);
        // }
        // });
    }

    @Scheduled(fixedDelay = 30000)
    public void shippingJobs() throws InterruptedException {
        List<ShipmentWaiting> shipmentWaitings = shipmentWaitingRepo.findAll();
        shipmentWaitings.forEach(shipmentWaiting -> {
            log.info("Shipping purchaseOrder ---> {}", shipmentWaiting);
            if (Duration.between(shipmentWaiting.getCreateTime(), Instant.now()).toSeconds() > 60) {
                PurchaseOrder order = orderRepository.findById(shipmentWaiting.getOrderId()).get();
                Optional<Product> product = productRepository
                        .findById(order.getProductId());
                order.setCurrentOrderStatus(OrderStatus.ORDER_CANCELLED);
                ArrayList<OrderStatus> activity = order.getOrderStatusHistory();
                activity.add(OrderStatus.SHIPMENT_TIMED_OUT);
                activity.add(OrderStatus.ORDER_CANCELLED);
                order.setOrderStatusHistory(activity);
                InventoryProduct inventoryProduct = inventroyRepository
                        .findInventoryProductByProductId(order.getProductId());
                inventoryProduct.setStock(inventoryProduct.getStock() + 1);
                inventroyRepository.save(inventoryProduct);
                OrderEvent orderEvent = new OrderEvent(order.getId(), order.getProductId(),
                        order.getUserId(),
                        product.get().getPrice(), "", OrderStatus.SHIPMENT_TIMED_OUT);
                orderEventPublisher.sendMessageToPaymentService(orderEvent);
                shipmentWaitingRepo.delete(shipmentWaiting);
                orderRepository.save(order);
            }
        });
        // List<PurchaseOrder> purchaseOrder =
        // orderRepository.findByCurrentOrderStatusAndTime(
        // Instant.now().minusSeconds(120),
        // Instant.now(), OrderStatus.ORDER_FULFILLED);
        // log.info("Shipping purchaseOrder ---> {}", purchaseOrder);
        // purchaseOrder.forEach(order -> {
        // log.info("shipping time --> " + Duration.between(order.getCreateTime(),
        // Instant.now()).toSeconds());
        // if (Duration.between(order.getCreateTime(), Instant.now()).toSeconds() > 60)
        // {
        // log.info("order ---> {}", order);
        // Optional<Product> product = productRepository
        // .findById(order.getProductId());
        // order.setCurrentOrderStatus(OrderStatus.ORDER_CANCELLED);
        // ArrayList<OrderStatus> activity = order.getOrderStatusHistory();
        // activity.add(OrderStatus.SHIPMENT_TIMED_OUT);
        // activity.add(OrderStatus.ORDER_CANCELLED);
        // order.setOrderStatusHistory(activity);
        // InventoryProduct inventoryProduct = inventroyRepository
        // .findInventoryProductByProductId(order.getProductId());
        // inventoryProduct.setStock(inventoryProduct.getStock() + 1);
        // inventroyRepository.save(inventoryProduct);
        // OrderEvent orderEvent = new OrderEvent(order.getId(), order.getProductId(),
        // order.getUserId(),
        // product.get().getPrice(), "", OrderStatus.SHIPMENT_TIMED_OUT);
        // orderEventPublisher.sendMessageToPaymentService(orderEvent);
        // orderRepository.save(order);
        // }
        // });
    }
}
