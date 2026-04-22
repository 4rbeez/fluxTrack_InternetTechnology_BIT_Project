package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.business.ProductService;
import com.example.demo.data.domain.Product;

@RestController
@RequestMapping("/")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    @GetMapping(path = "/product/", produces = "application/json")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    
}
