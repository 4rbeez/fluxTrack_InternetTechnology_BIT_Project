package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.business.OrderService;
import com.example.demo.data.domain.Order;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * List all orders visible to the authenticated user.
     * Admin sees all; partners see their own.
     */
    @GetMapping("/")
    public List<Order> getAllOrders(Authentication auth) {
        return orderService.getOrdersForUser(auth);
    }

    /**
     * Record a sale: creates an Order and decrements product stock atomically.
     * Body: { "productID": 1, "quantity": 1 }
     */
    @PostMapping(path = "/sale", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Order> recordSale(@RequestBody SaleRequest request, Authentication auth) {
        try {
            Order order = orderService.createOrderForSale(
                request.getProductID(),
                request.getQuantity(),
                auth
            );
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }
    }

    /** DTO for POST /order/sale */
    public static class SaleRequest {
        private Long productID;
        private Integer quantity;

        public Long getProductID() { return productID; }
        public void setProductID(Long productID) { this.productID = productID; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
