package com.example.demo.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.data.domain.Product;
import com.example.demo.data.repository.ProductRepository;

import java.util.List;

@Service
public class ProductService {
    
     @Autowired
    private ProductRepository productRepository;

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get a product by its ID
    public Product getProductById(Long id) {
       try {
        Product product = productRepository.findByProductID(id);
       return product;
       }
         catch (Exception e) {
          throw new RuntimeException("Failed to retrieve product with id: " + id, e);
         }
    }

    // Add a new product
    public Product addProduct(Product product) throws Exception {
        if (product.getProductName() == null || product.getProductName().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (product.getProductPartnerID() == null) {
            throw new IllegalArgumentException("Product must belong to a partner");
        }
        if (product.getProductSKU() == null || product.getProductSKU().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be null or empty");
        }
        if (product.getProductPrice() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        if (product.getProductQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
        else {
            return productRepository.save(product);
        }
    }

    // Update an existing product
    public Product updateProduct(Long id, Product updatedProduct) throws Exception {
        Product existingProduct = productRepository.findByProductID(id);
        if (existingProduct == null) {
            throw new IllegalArgumentException("Product with id " + id + " not found");
        }
        if (updatedProduct.getProductName() != null) {
            if (updatedProduct.getProductName().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
            existingProduct.setProductName(updatedProduct.getProductName());
        }
        if (updatedProduct.getProductPartnerID() != null) {
            existingProduct.setProductPartnerID(updatedProduct.getProductPartnerID());
        }
        if (updatedProduct.getProductSKU() != null) {
            if (updatedProduct.getProductSKU().isEmpty()) {
                throw new IllegalArgumentException("Product SKU cannot be empty");
            }
            existingProduct.setProductSKU(updatedProduct.getProductSKU());
        }
        if (updatedProduct.getProductPrice() != null) {
            if (updatedProduct.getProductPrice() < 0) {
                throw new IllegalArgumentException("Product price cannot be negative");
            }
            existingProduct.setProductPrice(updatedProduct.getProductPrice());
        }
        if (updatedProduct.getProductQuantity() != null) {
            if (updatedProduct.getProductQuantity() < 0) {
                throw new IllegalArgumentException("Product quantity cannot be negative");
            }
            existingProduct.setProductQuantity(updatedProduct.getProductQuantity());
        }
        try {
            return productRepository.save(existingProduct);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product with id: " + id, e);
        }
    }

    // Delete a product by its ID
    public void deleteProduct(Long id) throws Exception {
        Product existingProduct = productRepository.findByProductID(id);
        if (existingProduct == null) {
            throw new IllegalArgumentException("Product with id " + id + " not found");
        }
        try {
            productRepository.delete(existingProduct);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product with id: " + id, e);
        }
    }
}