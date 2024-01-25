package com.order.orderservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.order.orderservice.Dto.OrderStatus;
import com.order.orderservice.entity.InventoryProduct;
import com.order.orderservice.entity.Product;
import com.order.orderservice.exception.BusinessException;
import com.order.orderservice.repo.InventroyRepository;
import com.order.orderservice.repo.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InventroyService {
    @Autowired
    private InventroyRepository inventryRepo;
    @Autowired
    private ProductRepository productRepository;

    public InventoryProduct getInventoryProductById(String productId) {
        return inventryRepo.findInventoryProductByProductId(productId);
    }

    public List<InventoryProduct> getAllInventoryProducts() {
        return inventryRepo.findAll();
    }

    public ResponseEntity<InventoryProduct> createInventoryProduct(InventoryProduct inventoryProduct)
            throws BusinessException {
        Optional<Product> product = productRepository.findById(inventoryProduct.getProductId());
        if (!product.isPresent()) {
            throw new BusinessException("not_found", "product not found", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.status(HttpStatus.OK).body(inventryRepo.save(inventoryProduct));
    }

    public InventoryProduct updateInventoryProduct(InventoryProduct inventoryProduct) {
        return inventryRepo.save(inventoryProduct);
    }

    public OrderStatus processOrder(String productId) {
        try {
            InventoryProduct inventoryProduct = inventryRepo.findInventoryProductByProductId(productId);
            if (inventoryProduct.getStock() > 0) {
                inventoryProduct.setStock(inventoryProduct.getStock() - 1);
                inventryRepo.save(inventoryProduct);
                return OrderStatus.ORDER_FULFILLED;
            } else {
                log.info("InventroyService: Product with productId:{} , currently ITEM_OUT_OF_STOCK", productId);
                return OrderStatus.ITEM_OUT_OF_STOCK;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return OrderStatus.ITEM_OUT_OF_STOCK;
    }
}
