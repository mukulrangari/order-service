package com.order.orderservice.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.order.orderservice.entity.PaymentWaiting;

@Repository
public interface PaymentWaitingRepo extends MongoRepository<PaymentWaiting, String> {
    PaymentWaiting findByOrderId(String orderId);
}
