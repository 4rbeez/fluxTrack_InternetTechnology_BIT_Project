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
    // - Admin can delete any product
    // - Partner users can only delete products they own
    // Returns false if the deletion was denied or the product does not exist.
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
            return false; // Partner trying to delete someone else's product
        }
        productRepository.deleteById(productId);
        return true;
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