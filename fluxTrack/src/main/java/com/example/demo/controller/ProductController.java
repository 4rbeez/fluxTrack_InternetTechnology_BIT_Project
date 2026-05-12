package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.business.ProductService;
import com.example.demo.data.domain.Product;

@RestController
@RequestMapping("/product")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    // Get All Products
    @GetMapping(path = "/", produces = "application/json")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // Get Product by ID
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with given id: " + id, e);
        } 
    }

    // Add Product 
    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        try {
            product = productService.addProduct(product);
            return ResponseEntity.ok(product);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to add product: " + e.getMessage(), e);
        }
        
    }

    // Update Product
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            product = productService.updateProduct(id, product);
            return ResponseEntity.ok(product);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with given id: " + id, e);
        }
    }

    // Delete Product
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build(); 
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with given id: " + id, e);
        }
    }

}
