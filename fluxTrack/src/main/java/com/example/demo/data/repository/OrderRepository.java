package com.example.demo.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.data.domain.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Returns all orders belonging to a specific partner.
     * Used by OrderService.getOrdersForUser() to enforce role-based filtering.
     */
    List<Order> findByPartnerID(Long partnerID);
}
