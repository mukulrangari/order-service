package com.order.orderservice.Dto;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private String message;
    private String orderId;
}
