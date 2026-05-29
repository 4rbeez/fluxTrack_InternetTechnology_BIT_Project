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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;



import com.example.demo.business.ProductService;
import com.example.demo.data.domain.Product;

@RestController
@RequestMapping("/product")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    // Get All Products — returns the full role-scoped list.
    // Still used by the Dashboard, Reports, and Partners pages that
    // need every visible product, not a page slice.
    @GetMapping("/")
    public List<Product> getAllProducts(Authentication auth) {
        return productService.getProductsForUser(auth);
    }

    /**
     * Server-side paginated product list with optional filters.
     *
     * Used by the Products page. Search matches productName OR productSKU
     * (case-insensitive); filter accepts "instock" or "outofstock".
     * Role-based scoping is enforced inside the service — partner users
     * never see another partner's products even if they craft the URL.
     */
    @GetMapping(path = "/page", produces = "application/json")
    public PagedResponse<Product> getProductsPage(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter) {
        // Cap the page size so a malicious caller can't request an unbounded result.
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "productID"));
        Page<Product> result = productService.getProductsPaged(auth, search, filter, pageable);
        return PagedResponse.from(result);
    }

    // Get Product by ID
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with given id: " + id, e);
        } 
    }

    // Add Product
    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Product> addProduct(@RequestBody Product product, Authentication auth) {
        try {
            // BUSINESS RULE: partner users can only create products for themselves.
            Long forcedPartnerId = productService.resolvePartnerIdForUser(auth);
            if (forcedPartnerId != null) {
                product.setProductPartnerID(forcedPartnerId);
            } else if (product.getProductPartnerID() == null) {
                product.setProductPartnerID(1L);
            }

            product = productService.addProduct(product);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Failed to add product: " + e.getMessage(),
                e
            );
        }
    }

    // Update Product
    // Ownership-protected: partners can only update their own products;
    // admin can update any. Returns 403 if the caller doesn't own the
    // product or it doesn't exist. Returns 400 if the payload is invalid.
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            Authentication auth) {
        Product updated;
        try {
            updated = productService.updateProductForUser(id, product, auth);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update this product");
    }

    // Delete Product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication auth) {
        boolean deleted = productService.deleteProductForUser(id, auth);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete this product");
    }

}
