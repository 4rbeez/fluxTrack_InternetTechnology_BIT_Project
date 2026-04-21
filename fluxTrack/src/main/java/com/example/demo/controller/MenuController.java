package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.business.MenuService;
import com.example.demo.data.domain.Partner;
import com.example.demo.data.domain.Product;


@RestController
@RequestMapping(path = "/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

/* --- Product Endpoints --- */

    // Get Product by ID
    @GetMapping(path = "/product/{id}", produces = "application/json")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = menuService.getProductById(id);
            return ResponseEntity.ok(product);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with given id: " + id, e);
        }
    }

    // Get All Products
    @GetMapping(path = "/product/", produces = "application/json")
    public List<Product> getAllProducts() {
        return menuService.getAllProducts();
    }

    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        try {
            product = menuService.addProduct(product);
            return ResponseEntity.ok(product);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to add product: " + e.getMessage(), e);
        }
        
    }

    // Update Product
    @PutMapping(path = "/product/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            product = menuService.updateProduct(id, product);
            return ResponseEntity.ok(product);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with given id: " + id, e);
        }
    }

    // Delete Product
    @DeleteMapping(path = "/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            menuService.deleteProduct(id);
            return ResponseEntity.ok().build(); 
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with given id: " + id, e);
        }
    }

/* --- Partner Endpoints --- */

    // Get Partner by ID
    @GetMapping(path = "/partner/{id}", produces = "application/json")
    public ResponseEntity<Partner> getPartnerById(@PathVariable Long id) {
        try {
            Partner partner = menuService.getPartnerById(id);
            return ResponseEntity.ok(partner);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found with given id: " + id, e);
        }
    }

    // Get All Partners
    @GetMapping(path = "/partner/", produces = "application/json")
    public List<Partner> getAllPartners() {
        return menuService.getAllPartners();
    }

    // Add Partner
    @PostMapping(path = "/partner/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Partner> addPartner(@RequestBody Partner partner) {
        try {
            partner = menuService.addPartner(partner);
            return ResponseEntity.ok(partner);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to add partner: " + e.getMessage(), e);
        }
        
    }

    // Update Partner
    @PutMapping(path = "/partner/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Partner> updatePartner(@PathVariable Long id, @RequestBody Partner partner) {
        try {
            partner = menuService.updatePartner(id, partner);
            return ResponseEntity.ok(partner);
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found with given id: " + id, e);
        }
    }

    // Delete Partner
    @DeleteMapping(path = "/partner/{id}")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        try {
            menuService.deletePartner(id);
            return ResponseEntity.ok().build(); 
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found with given id: " + id, e);
        }
    }

}