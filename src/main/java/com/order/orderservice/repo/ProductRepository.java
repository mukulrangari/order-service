package com.order.orderservice.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.order.orderservice.entity.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

}
