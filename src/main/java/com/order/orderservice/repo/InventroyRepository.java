package com.order.orderservice.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.order.orderservice.entity.InventoryProduct;

@Repository
public interface InventroyRepository extends MongoRepository<InventoryProduct, String> {

    InventoryProduct findInventoryProductByProductId(String productId);
}
