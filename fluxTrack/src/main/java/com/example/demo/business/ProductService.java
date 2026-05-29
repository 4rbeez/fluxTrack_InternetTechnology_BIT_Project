package com.example.demo.business;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.data.domain.Product;
import com.example.demo.data.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // -----------------------------------------------------------------
    // BUSINESS RULE 1 (UC 301 - Show product overview)
    // -----------------------------------------------------------------
    // - User "admin" sees ALL products
    // - Partner users see ONLY their own products
    // -----------------------------------------------------------------
    public List<Product> getProductsForUser(Authentication auth) {
        if (auth == null) {
            return List.of();
        }
        String username = auth.getName();
        if ("admin".equals(username)) {
            return productRepository.findAll();
        }
        Long partnerId = resolvePartnerIdFromUsername(username);
        if (partnerId == null) {
            return List.of();
        }
        return productRepository.findByProductPartnerID(partnerId);
    }

    /**
     * Used by ProductController.addProduct() to enforce partner ownership on create.
     * Returns null for admin (admin must specify partner ID explicitly).
     */
    public Long resolvePartnerIdForUser(Authentication auth) {
        if (auth == null) return null;
        String username = auth.getName();
        if ("admin".equals(username)) return null;
        return resolvePartnerIdFromUsername(username);
    }

    private Long resolvePartnerIdFromUsername(String username) {
        if (username == null) return null;
        switch (username) {
            case "wylaade":     return 1L;
            case "drachehoehli": return 2L;
            default:            return null;
        }
    }

    // -----------------------------------------------------------------
    // BUSINESS RULE 2 (UC 5 - Delete a Product)
    // -----------------------------------------------------------------
    public boolean deleteProductForUser(Long productId, Authentication auth) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;
        if (auth == null) return false;

        String username = auth.getName();
        if ("admin".equals(username)) {
            productRepository.deleteById(productId);
            return true;
        }

        Long callerPartnerId = resolvePartnerIdFromUsername(username);
        if (callerPartnerId == null) return false;
        if (!callerPartnerId.equals(product.getProductPartnerID())) {
            return false;
        }
        productRepository.deleteById(productId);
        return true;
    }

    // -----------------------------------------------------------------
    // Ownership-protected update (mirrors UC 5 / deleteProductForUser)
    // -----------------------------------------------------------------
    public Product updateProductForUser(Long id, Product updated, Authentication auth) {
        if (auth == null) return null;
        Product existing = productRepository.findById(id).orElse(null);
        if (existing == null) return null;

        String username = auth.getName();
        if (!"admin".equals(username)) {
            Long callerPartnerId = resolvePartnerIdFromUsername(username);
            if (callerPartnerId == null) return null;
            if (!callerPartnerId.equals(existing.getProductPartnerID())) {
                return null;
            }
            updated.setProductPartnerID(callerPartnerId);
        }
        return updateProduct(id, updated);
    }

    // -----------------------------------------------------------------
    // Server-side pagination + filtering
    // -----------------------------------------------------------------
    // Returns one page of products visible to the caller. Applies the
    // role-based visibility filter from Rule 1, then layers an optional
    // search term (matches productName OR productSKU, case-insensitive)
    // and stock-status filter. All predicates are composed into a JPA
    // Specification so the database does the filtering and pagination —
    // we never load the full inventory into memory.
    // -----------------------------------------------------------------
    public Page<Product> getProductsPaged(Authentication auth, String search, String filter, Pageable pageable) {
        if (auth == null) return Page.empty(pageable);
        String username = auth.getName();

        // Start with an always-true predicate so we can .and() unconditionally.
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        // Role-based scope (UC 301)
        if (!"admin".equals(username)) {
            Long partnerId = resolvePartnerIdFromUsername(username);
            if (partnerId == null) return Page.empty(pageable);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("productPartnerID"), partnerId));
        }

        // Search: name OR SKU
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("productName")), pattern),
                cb.like(cb.lower(root.get("productSKU")), pattern)
            ));
        }

        // Stock-status filter
        if ("instock".equalsIgnoreCase(filter)) {
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("productQuantity"), 0));
        } else if ("outofstock".equalsIgnoreCase(filter)) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("productQuantity"), 0));
        }

        return productRepository.findAll(spec, pageable);
    }

    // -----------------------------------------------------------------
    // Payload validation — applied on both create and update.
    // The frontend already blocks negative values via input[min=0], but
    // a hand-crafted request can bypass that. Throws IllegalArgumentException,
    // which the controllers translate to HTTP 400.
    // -----------------------------------------------------------------
    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product payload is required");
        }
        if (product.getProductPrice() != null && product.getProductPrice() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        if (product.getProductQuantity() != null && product.getProductQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
    }

    // -----------------------------------------------------------------
    // CRUD methods
    // -----------------------------------------------------------------
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product addProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updated) {
        Product existing = productRepository.findById(id).orElse(null);
        if (existing == null) return null;
        validateProduct(updated);
        existing.setProductName(updated.getProductName());
        existing.setProductSKU(updated.getProductSKU());
        existing.setProductPrice(updated.getProductPrice());
        existing.setProductQuantity(updated.getProductQuantity());
        if (updated.getProductPartnerID() != null) {
            existing.setProductPartnerID(updated.getProductPartnerID());
        }
        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
