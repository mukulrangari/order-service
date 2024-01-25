package com.order.orderservice.repo;

import org.springframework.stereotype.Repository;
import com.order.orderservice.entity.ShipmentWaiting;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface ShipmentWaitingRepo extends MongoRepository<ShipmentWaiting, String> {
    ShipmentWaiting findByOrderId(String orderId);
}