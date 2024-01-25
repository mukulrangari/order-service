package com.order.orderservice.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PaymentWaiting {
    @Id
    private String id;
    private String orderId;
    private Instant createTime;
}
