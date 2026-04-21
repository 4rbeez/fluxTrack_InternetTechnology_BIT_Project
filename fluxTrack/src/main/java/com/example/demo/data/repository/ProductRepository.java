package com.example.demo.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.data.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByProductID(Long productID);
    List<Product> findAllByProductName(String productName);
    List<Product> findAllByProductSKU(String productSKU);
}