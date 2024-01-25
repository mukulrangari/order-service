package com.order.orderservice.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.order.orderservice.Dto.OrderEvent;
import com.order.orderservice.Dto.OrderRequestDto;
import com.order.orderservice.Dto.OrderResponseDto;
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
public class OrderService {
    @Autowired
    private InventroyRepository inventryRepo;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventroyRepository inventroyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private InventroyService inventroyService;
    @Autowired
    private ShipmentWaitingRepo shipmentWaitingRepo;

    @Autowired
    private PaymentWaitingRepo paymentWaitingRepo;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        try {
            // Check if product is product is present in inverntory/product
            Optional<Product> product = productRepository
                    .findById(orderRequestDto.getProductId());

            // Will create order with Initials order status ORDER_RECEIVED
            PurchaseOrder order = orderRepository.save(convertDtoToEntity(orderRequestDto));
            InventoryProduct inventoryProduct = inventroyRepository
                    .findInventoryProductByProductId(orderRequestDto.getProductId());
            if (inventoryProduct.getStock() > 0) {
                // produce kafka event with status ITEM_IN_STOCK
                OrderStatus currenOrderStatus = OrderStatus.ITEM_IN_STOCK;
                order.setCurrentOrderStatus(currenOrderStatus);
                ArrayList<OrderStatus> orderStatusHistory = order.getOrderStatusHistory();
                orderStatusHistory.add(currenOrderStatus);
                order.setOrderStatusHistory(orderStatusHistory);
                OrderEvent orderEvent = new OrderEvent(order.getId(), order.getProductId(), order.getUserId(),
                        product.get().getPrice(),
                        "",
                        order.getCurrentOrderStatus());
                PaymentWaiting paymentWaiting = new PaymentWaiting(null, order.getId(), Instant.now());
                paymentWaitingRepo.save(paymentWaiting);
                // Publishing to kafka payment-topic for payment service
                log.info("OrderService: Publishing event payment-topic for payment with event: {}", orderEvent);
                orderEventPublisher.sendMessageToPaymentService(orderEvent);

                // Saving update order
                orderRepository.save(order);
                return new OrderResponseDto("Order Accepted", order.getId());
            } else {
                // order with status ITEM_OUT_OF_STOCK
                order.setCurrentOrderStatus(OrderStatus.ORDER_CANCELLED);
                ArrayList<OrderStatus> activity = order.getOrderStatusHistory();
                activity.add(OrderStatus.ITEM_OUT_OF_STOCK);
                activity.add(OrderStatus.ORDER_CANCELLED);
                order.setOrderStatusHistory(activity);
                OrderEvent orderEvent = new OrderEvent(order.getId(), order.getProductId(),
                        order.getUserId(),
                        product.get().getPrice(),
                        "",
                        order.getCurrentOrderStatus());
                orderEventPublisher.sendMessageToPaymentService(orderEvent);
                orderRepository.save(order);
                return new OrderResponseDto("Item out of stock", order.getId());
            }
        } catch (Exception e) {
            log.error("OrderService: error ---> {}", e.getMessage());
            return new OrderResponseDto(e.getMessage(), null);
        }
    }

    public List<PurchaseOrder> getAllOrders() {
        log.info("OrderSercice: Getting all orders...");
        return orderRepository.findAll();
    }

    private PurchaseOrder convertDtoToEntity(OrderRequestDto dto) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setProductId(dto.getProductId());
        purchaseOrder.setUserId(dto.getUserId());
        purchaseOrder.setId(purchaseOrder.getId());
        purchaseOrder.setCurrentOrderStatus(OrderStatus.ORDER_RECEIVED);
        ArrayList<OrderStatus> activity = new ArrayList<>();
        activity.add(OrderStatus.ORDER_RECEIVED);
        purchaseOrder.setOrderStatusHistory(activity);
        purchaseOrder.setCreateTime(Instant.now());
        purchaseOrder.setUpdateTime(Instant.now());
        return purchaseOrder;
    }

    public List<PurchaseOrder> getOrdersByUserId(Integer userId) {
        return orderRepository.findByUserId(userId);
    }

    public Optional<PurchaseOrder> getOrderByOrderId(String id) {
        return orderRepository.findById(id);
    }

    public void cancelOrder(OrderEvent orderEvent) {
        log.info("OrderService: {} for orderId:{}", orderEvent.getOrderStatus(), orderEvent.getOrderId());
        PurchaseOrder purchaseOrder = orderRepository.findById(orderEvent.getOrderId()).orElse(null);
        purchaseOrder.setCurrentOrderStatus(OrderStatus.ORDER_CANCELLED);
        ArrayList<OrderStatus> activity = purchaseOrder.getOrderStatusHistory();
        activity.add(orderEvent.getOrderStatus());
        if (!activity.contains(OrderStatus.ORDER_CANCELLED)) {
            activity.add(OrderStatus.ORDER_CANCELLED);
        }
        if (orderEvent.getOrderStatus().equals(OrderStatus.PAYMENT_FAILED)) {
            PaymentWaiting paymentWaiting = paymentWaitingRepo.findByOrderId(orderEvent.getOrderId());
            paymentWaitingRepo.delete(paymentWaiting);
        }
        purchaseOrder.setOrderStatusHistory(activity);
        log.warn("OrderService: ORDER_CANCELLED for orderId:{}", orderEvent.getOrderStatus(), orderEvent.getOrderId());
        orderRepository.save(purchaseOrder);
    }

    public ResponseEntity<OrderResponseDto> cancelOrderRequest(String orderId) {
        PurchaseOrder purchaseOrder = orderRepository.findById(orderId).orElse(null);
        if (purchaseOrder.getOrderStatusHistory().contains(OrderStatus.ORDER_COMPLETED)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OrderResponseDto("Order already completed, So can not cancel", orderId));
        }
        Optional<Product> product = productRepository
                .findById(purchaseOrder.getProductId());
        final String uriString = "http://localhost:8083/api/v1/shipping/cancel/" + orderId;
        RestTemplate restTemplate = new RestTemplate();
        OrderEvent orderEvent = new OrderEvent(orderId, purchaseOrder.getProductId(), purchaseOrder.getUserId(),
                product.get().getPrice(),
                "",
                OrderStatus.ORDER_CANCEL_REQUEST);
        // ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        // String stringEvent = "";
        // try {
        // stringEvent = ow.writeValueAsString(orderEvent);
        // log.info("stringEvent ---> {}", stringEvent);
        // } catch (JsonProcessingException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // HttpEntity<String> request = new HttpEntity<>(stringEvent, headers);
        OrderResponseDto cancelShipping = restTemplate.getForObject(uriString,
                OrderResponseDto.class);
        if (cancelShipping.getMessage().equals("Shipment canceled")) {
            InventoryProduct inventoryProduct = inventroyRepository
                    .findInventoryProductByProductId(purchaseOrder.getProductId());
            inventoryProduct.setStock(inventoryProduct.getStock() + 1);
            inventroyRepository.save(inventoryProduct);
            purchaseOrder.setCurrentOrderStatus(OrderStatus.ORDER_CANCELLED);
            ArrayList<OrderStatus> activity = purchaseOrder.getOrderStatusHistory();
            activity.add(OrderStatus.ORDER_CANCEL_REQUEST);
            activity.add(OrderStatus.SHIPMENT_CANCELLED);
            activity.add(OrderStatus.ORDER_CANCELLED);
            purchaseOrder.setOrderStatusHistory(activity);
            log.warn("OrderService: ORDER_CANCELLED for orderId:{}", orderId);
            orderEvent.setOrderStatus(OrderStatus.SHIPMENT_CANCELLED);
            orderEventPublisher.sendMessageToPaymentService(orderEvent);
            orderRepository.save(purchaseOrder);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new OrderResponseDto("Order cancelled", orderId));
    }

    public void shippingOrder(OrderEvent orderEvent) {
        log.info("OrderService: {} for orderId:{}", orderEvent.getOrderStatus(), orderEvent.getOrderId());
        PurchaseOrder purchaseOrder = orderRepository.findById(orderEvent.getOrderId()).orElse(null);
        purchaseOrder.setCurrentOrderStatus(OrderStatus.ORDER_SHIPPED);
        ArrayList<OrderStatus> activity = purchaseOrder.getOrderStatusHistory();
        activity.add(orderEvent.getOrderStatus());
        activity.add(OrderStatus.ORDER_SHIPPED);
        purchaseOrder.setOrderStatusHistory(activity);
        ShipmentWaiting shipmentWaiting = shipmentWaitingRepo.findByOrderId(orderEvent.getOrderId());
        shipmentWaitingRepo.delete(shipmentWaiting);
        log.warn("OrderService: ORDER_SHIPPED for orderId:{}", orderEvent.getOrderStatus(), orderEvent.getOrderId());
        orderRepository.save(purchaseOrder);
    }

    public void completeOrder(OrderEvent orderEvent) {
        log.info("OrderService: {} for orderId:{}", orderEvent.getOrderStatus(), orderEvent.getOrderId());
        PurchaseOrder purchaseOrder = orderRepository.findById(orderEvent.getOrderId()).orElse(null);
        purchaseOrder.setCurrentOrderStatus(OrderStatus.ORDER_COMPLETED);
        ArrayList<OrderStatus> activity = purchaseOrder.getOrderStatusHistory();
        activity.add(orderEvent.getOrderStatus());
        activity.add(OrderStatus.ORDER_COMPLETED);
        purchaseOrder.setOrderStatusHistory(activity);
        log.warn("OrderService: ORDER_COMPLETED for orderId:{}", orderEvent.getOrderStatus(), orderEvent.getOrderId());
        orderRepository.save(purchaseOrder);
    }

    public void processOrder(OrderEvent orderEvent) {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PurchaseOrder purchaseOrder = orderRepository.findById(orderEvent.getOrderId()).orElse(null);
        OrderStatus newOrderStatus = inventroyService.processOrder(orderEvent.getProductId());
        ArrayList<OrderStatus> activity = purchaseOrder.getOrderStatusHistory();
        log.info("OrderService: PAYMENT_COMPLETED for orderId:{}", orderEvent.getOrderId());
        if (newOrderStatus.equals(OrderStatus.ORDER_FULFILLED)) {
            purchaseOrder.setCurrentOrderStatus(OrderStatus.ORDER_FULFILLED);
            activity.add(OrderStatus.PAYMENT_COMPLETED);
            activity.add(OrderStatus.ORDER_FULFILLED);
            orderEvent.setOrderStatus(OrderStatus.ORDER_FULFILLED);
            ShipmentWaiting shipmentWaiting = new ShipmentWaiting(null, purchaseOrder.getId(), Instant.now());
            shipmentWaitingRepo.save(shipmentWaiting);
            PaymentWaiting paymentWaiting = paymentWaitingRepo.findByOrderId(orderEvent.getOrderId());
            paymentWaitingRepo.delete(paymentWaiting);
            log.info("OrderService: ORDER_FULFILLED for orderId:{}", orderEvent.getOrderId());
            orderEventPublisher.sendMessageToShippingService(orderEvent);
        } else {
            purchaseOrder.setCurrentOrderStatus(newOrderStatus);
            activity.add(OrderStatus.PAYMENT_COMPLETED);
            activity.add(newOrderStatus);
            orderEvent.setOrderStatus(newOrderStatus);
            log.info("OrderService: Publishing event payment-topic for payment with event: {}", orderEvent);
            orderEventPublisher.sendMessageToPaymentService(orderEvent);
        }
        purchaseOrder.setOrderStatusHistory(activity);
        orderRepository.save(purchaseOrder);
    }

    public void updateShipmentStatus(OrderEvent orderEvent) {
        PurchaseOrder purchaseOrder = orderRepository.findById(orderEvent.getOrderId()).orElse(null);
        ArrayList<OrderStatus> activity = purchaseOrder.getOrderStatusHistory();
        purchaseOrder.setCurrentOrderStatus(orderEvent.getOrderStatus());
        activity.add(orderEvent.getOrderStatus());
        ShipmentWaiting shipmentWaiting = shipmentWaitingRepo.findByOrderId(orderEvent.getOrderId());
        shipmentWaitingRepo.delete(shipmentWaiting);
        log.info("OrderService: Publishing event payment-topic for payment with event: {}", orderEvent);
        orderEventPublisher.sendMessageToPaymentService(orderEvent);
        InventoryProduct inventoryProduct = inventryRepo.findInventoryProductByProductId(orderEvent.getProductId());
        inventoryProduct.setStock(inventoryProduct.getStock() + 1);
        inventroyRepository.save(inventoryProduct);
        orderRepository.save(purchaseOrder);
    }
}
