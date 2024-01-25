package com.order.orderservice.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.order.orderservice.entity.PurchaseOrder;

import java.time.Instant;
import java.util.List;
import com.order.orderservice.Dto.OrderStatus;

@Repository
public interface OrderRepository extends MongoRepository<PurchaseOrder, String> {
    List<PurchaseOrder> findByUserId(Integer userId);

    @Query("{'createTime' : {$gte: ?0 ,$lte: ?1}}, 'currentOrderStatus' : ?2")
    List<PurchaseOrder> findByCurrentOrderStatusAndTime(Instant from, Instant to, OrderStatus currentOrderStatus);
}
