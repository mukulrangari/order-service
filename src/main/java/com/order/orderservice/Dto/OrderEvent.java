package com.order.orderservice.Dto;

import java.util.Date;
import java.util.UUID;

import com.order.orderservice.entity.OrderActivity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    String orderId;
    String productId;
    String userId;
    Integer amount;
    String transactionId;
    OrderStatus orderStatus;
}
