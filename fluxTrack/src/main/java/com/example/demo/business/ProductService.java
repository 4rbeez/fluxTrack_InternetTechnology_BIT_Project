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

}
