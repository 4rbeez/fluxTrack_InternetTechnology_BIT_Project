package com.example.demo.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.data.domain.Order;

/**
 * JpaSpecificationExecutor is what enables the paginated/filtered endpoint
 * to build query predicates dynamically (search term, date range, partner
 * filter, role-based ownership) and combine them with a Pageable in one call.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    /**
     * Returns all orders belonging to a specific partner.
     * Used by OrderService.getOrdersForUser() to enforce role-based filtering.
     */
    List<Order> findByPartnerID(Long partnerID);
}
