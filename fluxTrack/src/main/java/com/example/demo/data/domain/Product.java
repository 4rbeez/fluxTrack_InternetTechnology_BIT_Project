package com.example.demo.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_id", nullable = false)
    private Long productID;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_partner_id", nullable = false)
    private Long productPartnerId;

    @Column(name = "product_SKU", nullable = false, unique = true)
    private String productSKU;

    @Column(name = "product_price", nullable = false)
    private Double productPrice;

    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity;

    // Many-to-One relationship with Partner
    @ManyToOne
    private Partner partner;

    // Empty Constructor for JPA
    public Product() {
    }

    // Full Constructor - might not be needed
    public Product(Long productID, String productName, Long productPartnerId, String productSKU, Double productPrice, Integer productQuantity) {
        this.productID = productID;
        this.productName = productName;
        this.productPartnerId = productPartnerId;
        this.productSKU = productSKU;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
    }

    public Long getProductID() {
        return productID;
    }

    public void setProductID(Long productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getProductPartnerId() {
        return productPartnerId;
    }

    public void setProductPartnerId(Long productPartnerId) {
        this.productPartnerId = productPartnerId;
    }

    public String getProductSKU() {
        return productSKU;
    }

    public void setProductSKU(String productSKU) {
        this.productSKU = productSKU;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
    }

}
