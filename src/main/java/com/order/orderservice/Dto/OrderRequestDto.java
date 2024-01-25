package com.order.orderservice.Dto;

import org.bson.types.ObjectId;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {
    private String userId;
    private String productId;
}
