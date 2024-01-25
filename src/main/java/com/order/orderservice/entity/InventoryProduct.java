package com.order.orderservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("InventoryProduct")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryProduct {
    @Id
    private String id;
    private String productId;
    private Integer stock;
}
