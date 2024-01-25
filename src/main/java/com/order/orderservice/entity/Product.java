package com.order.orderservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Document("Product")
public class Product {
    @Id
    private String id;
    private String name;
    private Integer price;
}
