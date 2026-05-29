package com.example.demo.business;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.data.domain.Order;
import com.example.demo.data.domain.Product;
import com.example.demo.data.repository.OrderRepository;
import com.example.demo.data.repository.ProductRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    // -----------------------------------------------------------------
    // BUSINESS RULE (UC 304 - View order history)
    // -----------------------------------------------------------------
    // - User "admin" sees ALL orders across the system
    // - Partner users see ONLY their own orders
    // -----------------------------------------------------------------
    public List<Order> getOrdersForUser(Authentication auth) {
        if (auth == null) return List.of();
        String username = auth.getName();
        if ("admin".equals(username)) {
            return orderRepository.findAll();
        }
        Long partnerId = resolvePartnerIdFromUsername(username);
        if (partnerId == null) return List.of();
        return orderRepository.findByPartnerID(partnerId);
    }

    // -----------------------------------------------------------------
    // BUSINESS RULE (Record-a-Sale)
    // -----------------------------------------------------------------
    // Decrementing a product's stock through the inventory UI creates an
    // Order record. This atomically:
    //   1. Validates: quantity > 0 and not greater than current stock
    //   2. Validates: caller owns the product (admin bypasses)
    //   3. Creates an Order with productName + totalAmount snapshot
    //   4. Decrements the product's stock quantity
    // -----------------------------------------------------------------
    public Order createOrderForSale(Long productId, Integer quantity, Authentication auth) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        if (product.getProductQuantity() == null || product.getProductQuantity() < quantity) {
            throw new IllegalStateException("Not enough stock");
        }

        if (auth != null) {
            String username = auth.getName();
            if (!"admin".equals(username)) {
                Long callerPartnerId = resolvePartnerIdFromUsername(username);
                if (callerPartnerId == null
                    || !callerPartnerId.equals(product.getProductPartnerID())) {
                    throw new SecurityException("Cannot record sale for another partner's product");
                }
            }
        }

        Order order = new Order(
            product.getProductID(),
            product.getProductName(),
            product.getProductPartnerID(),
            quantity,
            product.getProductPrice() * quantity,
            LocalDateTime.now()
        );
        orderRepository.save(order);

        product.setProductQuantity(product.getProductQuantity() - quantity);
        productRepository.save(product);

        return order;
    }

    // -----------------------------------------------------------------
    // Server-side pagination + filtering
    // -----------------------------------------------------------------
    // Returns one page of orders visible to the caller, with optional
    // search (matches productName), date range, and (admin-only) partner
    // filter. The Specification is built by buildOrderSpec() and shared
    // with getOrdersSummary() so the summary card on the Order History
    // page always reflects the same filtered set as the visible rows.
    // -----------------------------------------------------------------
    public Page<Order> getOrdersPaged(Authentication auth, String search, String dateFrom,
                                      String dateTo, Long partnerFilter, Pageable pageable) {
        Specification<Order> spec = buildOrderSpec(auth, search, dateFrom, dateTo, partnerFilter);
        if (spec == null) return Page.empty(pageable);
        return orderRepository.findAll(spec, pageable);
    }

    /**
     * Aggregations (count, total units sold, total revenue) for the same
     * filtered set the paged endpoint returns. Computed in-memory for
     * simplicity — for an academic project's data volume this is fine.
     * A production system would push this down to the database with a
     * dedicated COUNT/SUM query.
     */
    public OrderSummary getOrdersSummary(Authentication auth, String search, String dateFrom,
                                         String dateTo, Long partnerFilter) {
        Specification<Order> spec = buildOrderSpec(auth, search, dateFrom, dateTo, partnerFilter);
        if (spec == null) return new OrderSummary(0L, 0L, 0.0);

        List<Order> matching = orderRepository.findAll(spec);
        long count = matching.size();
        long totalUnits = matching.stream()
            .mapToLong(o -> o.getQuantity() != null ? o.getQuantity() : 0L)
            .sum();
        double totalRevenue = matching.stream()
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
            .sum();
        return new OrderSummary(count, totalUnits, totalRevenue);
    }

    /**
     * Shared spec builder for paged + summary endpoints. Returns null if
     * the caller has no valid scope (e.g. unknown partner) — callers
     * translate this to an empty result.
     */
    private Specification<Order> buildOrderSpec(Authentication auth, String search, String dateFrom,
                                                String dateTo, Long partnerFilter) {
        if (auth == null) return null;
        String username = auth.getName();
        boolean isAdmin = "admin".equals(username);

        Specification<Order> spec = (root, query, cb) -> cb.conjunction();

        if (!isAdmin) {
            // Partner users are locked to their own partnerID regardless of any
            // partner=X param they might try to send.
            Long partnerId = resolvePartnerIdFromUsername(username);
            if (partnerId == null) return null;
            spec = spec.and((root, query, cb) -> cb.equal(root.get("partnerID"), partnerId));
        } else if (partnerFilter != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("partnerID"), partnerFilter));
        }

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("productName")), pattern)
            );
        }

        if (dateFrom != null && !dateFrom.isBlank()) {
            LocalDateTime fromTime = LocalDate.parse(dateFrom).atStartOfDay();
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("orderDate"), fromTime)
            );
        }
        if (dateTo != null && !dateTo.isBlank()) {
            LocalDateTime toTime = LocalDate.parse(dateTo).atTime(23, 59, 59);
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("orderDate"), toTime)
            );
        }

        return spec;
    }

    // -----------------------------------------------------------------
    // Used by seed data to insert historical orders with a chosen date
    // -----------------------------------------------------------------
    public Order seedOrder(Product product, Integer quantity, LocalDateTime when) {
        Order order = new Order(
            product.getProductID(),
            product.getProductName(),
            product.getProductPartnerID(),
            quantity,
            product.getProductPrice() * quantity,
            when
        );
        return orderRepository.save(order);
    }

    private Long resolvePartnerIdFromUsername(String username) {
        if (username == null) return null;
        switch (username) {
            case "wylaade":      return 1L;
            case "drachehoehli": return 2L;
            default:             return null;
        }
    }

    /**
     * Aggregations returned by getOrdersSummary. Public so the controller can
     * serialize it directly to JSON.
     */
    public record OrderSummary(long count, long totalUnits, double totalRevenue) {}
}
