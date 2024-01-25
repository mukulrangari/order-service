package com.order.orderservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.order.orderservice.entity.Product;
import com.order.orderservice.repo.ProductRepository;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepo;

    public Product createProduct(Product product) {
        return productRepo.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Optional<Product> getProductById(String productId) {
        return productRepo.findById(productId);
    }
}
