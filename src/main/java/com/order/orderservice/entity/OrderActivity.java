package com.order.orderservice.entity;

import java.util.Date;

import com.order.orderservice.Dto.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderActivity {
    OrderStatus status;
    Date eventTime;
}
