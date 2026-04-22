// package com.example.demo.business;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// // import com.example.demo.data.domain.Partner;
// import com.example.demo.data.domain.Product;
// // import com.example.demo.data.repository.PartnerRepository;
// import com.example.demo.data.repository.ProductRepository;

// @Service
// public class MenuService {

//     @Autowired
//     private ProductRepository productRepository;

//     // @Autowired
//     // private PartnerRepository partnerRepository;

//     /* --- Product Operations --- */

//     // // Get a product by its ID
//     // public Product getProductById(Long id) {
//     //    try {
//     //     Product product = productRepository.findByProductID(id);
//     //    return product;
//     //    }
//     //      catch (Exception e) {
//     //       throw new RuntimeException("Failed to retrieve product with id: " + id, e);
//     //      }
//     // }

//     // Get all products
//     public List<Product> getAllProducts() {
//         return productRepository.findAll();
//     }

// //     // Add a new product
// //     public Product addProduct(Product product) throws Exception {
// //         if (product.getProductName() == null || product.getProductName().isEmpty()) {
// //             throw new IllegalArgumentException("Product name cannot be null or empty");
// //         }
// //         if (product.getProductPartnerId() == null) {
// //             throw new IllegalArgumentException("Product must belong to a partner");
// //         }
// //         if (product.getProductSKU() == null || product.getProductSKU().isEmpty()) {
// //             throw new IllegalArgumentException("Product SKU cannot be null or empty");
// //         }
// //         if (product.getProductPrice() < 0) {
// //             throw new IllegalArgumentException("Product price cannot be negative");
// //         }
// //         if (product.getProductQuantity() < 0) {
// //             throw new IllegalArgumentException("Product quantity cannot be negative");
// //         }
// //         else {
// //             return productRepository.save(product);
// //         }
// //     }

// //     // Update an existing product
// //     public Product updateProduct(Long id, Product updatedProduct) throws Exception {
// //         Product existingProduct = productRepository.findByProductID(id);
// //         if (existingProduct == null) {
// //             throw new IllegalArgumentException("Product with id " + id + " not found");
// //         }
// //         if (updatedProduct.getProductName() != null) {
// //             if (updatedProduct.getProductName().isEmpty()) {
// //                 throw new IllegalArgumentException("Product name cannot be empty");
// //             }
// //             existingProduct.setProductName(updatedProduct.getProductName());
// //         }
// //         if (updatedProduct.getProductPartnerId() != null) {
// //             existingProduct.setProductPartnerId(updatedProduct.getProductPartnerId());
// //         }
// //         if (updatedProduct.getProductSKU() != null) {
// //             if (updatedProduct.getProductSKU().isEmpty()) {
// //                 throw new IllegalArgumentException("Product SKU cannot be empty");
// //             }
// //             existingProduct.setProductSKU(updatedProduct.getProductSKU());
// //         }
// //         if (updatedProduct.getProductPrice() != null) {
// //             if (updatedProduct.getProductPrice() < 0) {
// //                 throw new IllegalArgumentException("Product price cannot be negative");
// //             }
// //             existingProduct.setProductPrice(updatedProduct.getProductPrice());
// //         }
// //         if (updatedProduct.getProductQuantity() != null) {
// //             if (updatedProduct.getProductQuantity() < 0) {
// //                 throw new IllegalArgumentException("Product quantity cannot be negative");
// //             }
// //             existingProduct.setProductQuantity(updatedProduct.getProductQuantity());
// //         }
// //         try {
// //             return productRepository.save(existingProduct);
// //         } catch (Exception e) {
// //             throw new RuntimeException("Failed to update product with id: " + id, e);
// //         }
// //     }

// //     // Delete a product by its ID
// //     public void deleteProduct(Long id) throws Exception {
// //         Product existingProduct = productRepository.findByProductID(id);
// //         if (existingProduct == null) {
// //             throw new IllegalArgumentException("Product with id " + id + " not found");
// //         }
// //         try {
// //             productRepository.delete(existingProduct);
// //         } catch (Exception e) {
// //             throw new RuntimeException("Failed to delete product with id: " + id, e);
// //         }
// //     }

// //     /* --- Partner Operations --- */

// //     // Get a partner by its ID
// //     public Partner getPartnerById(Long id) {
// //         try {
// //             Partner partner = partnerRepository.findByPartnerID(id);
// //             return partner;
// //         } catch (Exception e) {
// //             throw new RuntimeException("Failed to retrieve partner with id: " + id, e);
// //         }
// //     }

// //     // Get all partners
// //     public List<Partner> getAllPartners() {
// //         return partnerRepository.findAll();
// //     }

// //     // Add a new partner
// //     public Partner addPartner(Partner partner) throws Exception {
// //         if (partner.getPartnerName() == null || partner.getPartnerName().isEmpty()) {
// //             throw new IllegalArgumentException("Partner name cannot be null or empty");
// //         }
// //         if (partner.getPartnerEmail() == null || partner.getPartnerEmail().isEmpty()) {
// //             throw new IllegalArgumentException("Partner email cannot be null or empty");
// //         }
// //         else {
// //             return partnerRepository.save(partner);
// //         }
// //     }

// //     // Update an existing partner
// //     public Partner updatePartner(Long id, Partner updatedPartner) throws Exception {
// //         Partner existingPartner = partnerRepository.findByPartnerID(id);
// //         if (existingPartner == null) {
// //             throw new IllegalArgumentException("Partner with id " + id + " not found");
// //         }
// //         if (updatedPartner.getPartnerName() != null) {
// //             if (updatedPartner.getPartnerName().isEmpty()) {
// //                 throw new IllegalArgumentException("Partner name cannot be empty");
// //             }
// //             existingPartner.setPartnerName(updatedPartner.getPartnerName());
// //         }
// //         if (updatedPartner.getPartnerEmail() != null) {
// //             if (updatedPartner.getPartnerEmail().isEmpty()) {
// //                 throw new IllegalArgumentException("Partner email cannot be empty");
// //             }
// //             existingPartner.setPartnerEmail(updatedPartner.getPartnerEmail());
// //         }
// //         if (updatedPartner.getPartnerPhone() != null) {
// //             existingPartner.setPartnerPhone(updatedPartner.getPartnerPhone());
// //         }
// //         if (updatedPartner.getPartnerAddress() != null) {
// //             existingPartner.setPartnerAddress(updatedPartner.getPartnerAddress());
// //         }
// //         try {
// //             return partnerRepository.save(existingPartner);
// //         } catch (Exception e) {
// //             throw new RuntimeException("Failed to update partner with id: " + id, e);
// //         }
// //     }

// //     // Delete a partner by its ID
// //     public void deletePartner(Long id) throws Exception {
// //         Partner existingPartner = partnerRepository.findByPartnerID(id);
// //         if (existingPartner == null) {
// //             throw new IllegalArgumentException("Partner with id " + id + " not found");
// //         }
// //         try {
// //             partnerRepository.delete(existingPartner);
// //         } catch (Exception e) {
// //             throw new RuntimeException("Failed to delete partner with id: " + id, e);
// //         }
// //     }
        
// }