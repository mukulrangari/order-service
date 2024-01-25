package com.order.orderservice.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.orderservice.Dto.OrderEvent;
import com.order.orderservice.Dto.OrderStatus;
import com.order.orderservice.utils.KafkaUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderEventConsumer {
    @Autowired
    private EventHandler eventHandler;

    @KafkaListener(topics = KafkaUtils.TOPIC_ORDER, groupId = KafkaUtils.GROUP_ID)
    public void consumeFromPaymentService(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("OrderEventConsumer: Consuming message from payment service with event -> {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            OrderEvent orderEvent = new OrderEvent();
            orderEvent.setAmount(jsonNode.get("amount").asInt());
            orderEvent.setOrderId(jsonNode.get("orderId").asText());
            orderEvent.setProductId(jsonNode.get("productId").asText());
            orderEvent.setUserId(jsonNode.get("userId").asText());
            orderEvent.setOrderStatus(OrderStatus.valueOf(jsonNode.get("orderStatus").asText()));
            log.info("OrderEventConsumer: Processing event for Order update from payment service with event -> {}",
                    message);
            eventHandler.eventProcceser(orderEvent);
        } catch (JsonMappingException e) {
            log.error("OrderEventConsumer: Error --> {} while Consuming message from payment service with event -> {}",
                    e.getMessage(), message);
        }

    }

    @KafkaListener(topics = KafkaUtils.TOPIC_SHIPPING, groupId = KafkaUtils.GROUP_ID)
    public void consumeFromShippingService(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("OrderEventConsumer: Consuming message from shipping service with event -> {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            OrderEvent orderEvent = new OrderEvent();
            orderEvent.setAmount(jsonNode.get("amount").asInt());
            orderEvent.setOrderId(jsonNode.get("orderId").asText());
            orderEvent.setProductId(jsonNode.get("productId").asText());
            orderEvent.setUserId(jsonNode.get("userId").asText());
            orderEvent.setOrderStatus(OrderStatus.valueOf(jsonNode.get("orderStatus").asText()));
            log.info("OrderEventConsumer: Processing event for Order update from shipping service with event -> {}",
                    message);
            eventHandler.eventProcceser(orderEvent);
        } catch (JsonMappingException e) {
            log.error("OrderEventConsumer: Error --> {} while Consuming message from shipping service with event -> {}",
                    e.getMessage(), message);
        }

    }
}
