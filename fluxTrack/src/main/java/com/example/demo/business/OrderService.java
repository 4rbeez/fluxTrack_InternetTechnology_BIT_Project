package com.example.demo.business;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    //
    // Note: in production, bulk orders would arrive via the Shopify API
    // (UC 304 from the RE doc). The inventory UI only records single-unit
    // sales for this demonstrator.
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

        // Ownership check: partners can only record sales for their own products
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
}
