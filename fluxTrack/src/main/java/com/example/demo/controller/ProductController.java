package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    @GetMapping("/")
    public List<Product> getAllProducts(Authentication auth) {
        return productService.getProductsForUser(auth);
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
    public ResponseEntity<Product> addProduct(@RequestBody Product product, Authentication auth) {
        try {
            // BUSINESS RULE: partner users can only create products for themselves.
            // For partner users, this overrides any partnerID they might try to send.
            // For admin (returns null), allow whatever was in the body, defaulting to 1.
            Long forcedPartnerId = productService.resolvePartnerIdForUser(auth);
            if (forcedPartnerId != null) {
                product.setProductPartnerID(forcedPartnerId);
            } else if (product.getProductPartnerID() == null) {
                product.setProductPartnerID(1L);
            }

            product = productService.addProduct(product);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            // Validation failure from the service (e.g. negative price/quantity).
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Failed to add product: " + e.getMessage(),
                e
            );
        }
    }




    // @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    // public ResponseEntity<Product> addProduct(@RequestBody Product product) {
    //     try {
    //         product = productService.addProduct(product);
    //         return ResponseEntity.ok(product);
    //     } 
    //     catch (Exception e) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to add product: " + e.getMessage(), e);
    //     }
        
    // }

    // Update Product
    // Ownership-protected: partners can only update their own products;
    // admin can update any. Returns 403 if the caller doesn't own the
    // product or it doesn't exist (we don't leak existence to non-owners).
    // Returns 400 if the payload fails validation (e.g. negative price).
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            Authentication auth) {
        Product updated;
        try {
            updated = productService.updateProductForUser(id, product, auth);
        } catch (IllegalArgumentException e) {
            // Validation failure (e.g. negative price/quantity).
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update this product");
    }

    // Delete Product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication auth) {
        boolean deleted = productService.deleteProductForUser(id, auth);
        if (deleted) {
            return ResponseEntity.noContent().build();   // 204
        }
        // Either the product doesn't exist, or this user doesn't own it.
        // We return 403 for both cases so we don't leak existence info to non-owners.
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete this product");
    }

}