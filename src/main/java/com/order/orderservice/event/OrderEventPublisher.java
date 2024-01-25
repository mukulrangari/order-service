package com.order.orderservice.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.order.orderservice.Dto.OrderEvent;
import com.order.orderservice.utils.KafkaUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderEventPublisher {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessageToPaymentService(OrderEvent event) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String stringEvent = "";
        try {
            log.info("OrderEventPublisher: Publishing message to topic with event -> {}", event);
            stringEvent = ow.writeValueAsString(event);
            log.info("OrderEventPublisher: Published message to topic with event -> {}", event);
        } catch (JsonProcessingException e) {
            log.info("OrderEventPublisher: Error --> {} while publishing message to topic with event -> {}",
                    e.getMessage(), event);
        }

        kafkaTemplate.send(KafkaUtils.TOPIC_PAYMENT, stringEvent);
    }

    public void sendMessageToShippingService(OrderEvent event) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String stringEvent = "";
        try {
            log.info("OrderEventPublisher: Publishing message to topic with event -> {}", event);
            stringEvent = ow.writeValueAsString(event);
            log.info("OrderEventPublisher: Published message to topic with event -> {}", event);
        } catch (JsonProcessingException e) {
            log.info("OrderEventPublisher: Error --> {} while publishing message to topic with event -> {}",
                    e.getMessage(), event);
        }

        kafkaTemplate.send(KafkaUtils.TOPIC_SHIPPING, stringEvent);
    }
}
