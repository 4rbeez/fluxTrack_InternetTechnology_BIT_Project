package com.example.demo.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.data.domain.Product;

/**
 * JpaSpecificationExecutor is what enables the paginated/filtered endpoint
 * to build query predicates dynamically (search term, stock-status filter,
 * partner ownership) and combine them with a Pageable, all in one call.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByProductPartnerID(Long productPartnerID); // returns all products for a specific partner ID 
    // (foreign key in Product entity) for user based visibility
}
