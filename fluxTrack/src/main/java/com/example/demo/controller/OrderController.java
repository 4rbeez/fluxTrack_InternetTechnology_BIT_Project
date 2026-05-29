package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * Still used by the Reports page (full client-side aggregation).
     */
    @GetMapping("/")
    public List<Order> getAllOrders(Authentication auth) {
        return orderService.getOrdersForUser(auth);
    }

    /**
     * Server-side paginated order list with optional filters.
     *
     * Used by the Order History page. Supports search by productName,
     * a date range, and (admin only) a partner filter. Sorted newest first.
     */
    @GetMapping(path = "/page", produces = "application/json")
    public PagedResponse<Order> getOrdersPage(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long partner) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<Order> result = orderService.getOrdersPaged(auth, search, dateFrom, dateTo, partner, pageable);
        return PagedResponse.from(result);
    }

    /**
     * Aggregate totals (count, units, revenue) for the same filtered set
     * as /page. Used by the Order History summary card so that the totals
     * always reflect the full filtered range, not just the current page.
     */
    @GetMapping(path = "/summary", produces = "application/json")
    public OrderService.OrderSummary getOrdersSummary(
            Authentication auth,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long partner) {
        return orderService.getOrdersSummary(auth, search, dateFrom, dateTo, partner);
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
