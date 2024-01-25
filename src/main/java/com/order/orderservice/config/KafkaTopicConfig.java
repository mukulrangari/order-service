package com.order.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.order.orderservice.utils.KafkaUtils;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name(KafkaUtils.TOPIC_PAYMENT)
                .build();
    }

    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name(KafkaUtils.TOPIC_ORDER)
                .build();
    }

    @Bean
    public NewTopic shippingTopic() {
        return TopicBuilder.name(KafkaUtils.TOPIC_SHIPPING)
                .build();
    }
}