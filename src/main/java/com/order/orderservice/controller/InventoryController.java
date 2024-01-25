package com.order.orderservice.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.order.orderservice.entity.InventoryProduct;
import com.order.orderservice.entity.Product;
import com.order.orderservice.entity.PurchaseOrder;
import com.order.orderservice.service.InventroyService;
import com.order.orderservice.service.ProductService;

@RestController
@RequestMapping("api/v1")
public class InventoryController {
    @Autowired
    private InventroyService inventryService;
    @Autowired
    private ProductService productService;

    @PostMapping("/product")
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/product/{productId}")
    public Optional<Product> getProduct(@PathVariable String productId) {
        return productService.getProductById(productId);
    }

    @GetMapping("/inventory/products")
    public List<InventoryProduct> getInventoryProducts() {
        return inventryService.getAllInventoryProducts();
    }

    @PostMapping("/inventory/product")
    public ResponseEntity<InventoryProduct> createInventoryProduct(@RequestBody InventoryProduct product)
            throws Exception {
        return inventryService.createInventoryProduct(product);
    }

    @PutMapping("/inventory/product")
    public InventoryProduct updateInventoryProduct(@RequestBody InventoryProduct product) {
        return inventryService.updateInventoryProduct(product);
    }

    @GetMapping("/inventory/product/{productId}")
    public InventoryProduct getInventoryProduct(@PathVariable String productId) {
        return inventryService.getInventoryProductById(productId);
    }
}
