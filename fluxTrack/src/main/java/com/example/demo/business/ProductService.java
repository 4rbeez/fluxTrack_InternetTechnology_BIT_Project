package com.example.demo.business;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.data.domain.Product;
import com.example.demo.data.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // -----------------------------------------------------------------
    // BUSINESS RULE (UC 301 - Show product overview)
    // -----------------------------------------------------------------
    // - User "admin" sees ALL products
    // - Partner users see ONLY their own products
    //
    // We identify users by their JWT subject (username) rather than by
    // role, because the project's TokenService strips ROLE_* authorities
    // before encoding the JWT. The username survives as the subject claim.
    //
    // Partner-to-User linkage is currently hardcoded:
    //     "wylaade"     -> partnerID 1 (Wylaade GmbH)
    //     "drachehöhli" -> partnerID 2 (Drachehöhli GmbH)
    // This will be replaced by a proper User entity in a later iteration.
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
     * Returns the partner ID that products created by this user belong to.
     * Returns null for admin (admin must specify a partner ID explicitly).
     * Used by ProductController.addProduct() to enforce partner ownership.
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
    // CRUD methods (existing behaviour preserved)
    // -----------------------------------------------------------------

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updated) {
        Product existing = productRepository.findById(id).orElse(null);
        if (existing == null) return null;
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