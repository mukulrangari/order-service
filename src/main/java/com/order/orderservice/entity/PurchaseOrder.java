package com.order.orderservice.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.order.orderservice.Dto.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("Order")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrder {
    @Id
    private String id;
    private String userId;
    private String productId;
    private OrderStatus currentOrderStatus;
    ArrayList<OrderStatus> orderStatusHistory;
    private Instant createTime;
    private Instant updateTime;
}
