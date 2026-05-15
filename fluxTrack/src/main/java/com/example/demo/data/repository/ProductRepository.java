package com.example.demo.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.data.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductPartnerID(Long productPartnerID); // returns all products for a specific partner ID 
    // (foreign key in Product entity) for user based visibility
}