package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.business.PartnerService;
import com.example.demo.business.ProductService;
import com.example.demo.data.domain.Address;
import com.example.demo.data.domain.Partner;
import com.example.demo.data.domain.Product;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@RestController
public class fluxTrackApplication {

	@Autowired
	private ProductService productService;
	@Autowired
	private PartnerService partnerService;

	public static void main(String[] args) {
		SpringApplication.run(fluxTrackApplication.class, args);
	}
	
		// Simple endpoint to test if the application is running
	    @GetMapping("/welcome")
   		public String getWelcomeString() {
        return "Hello World";
		}

		// Initialize some test data for testing purposes
		@PostConstruct
		public void initTestData() throws Exception {

			Partner testPartner = new Partner();
			testPartner.setPartnerName("Test Partner");
			testPartner.setPartnerEmail("partner@example.com");
			testPartner.setPartnerPhone("123-456-7890");

			Address testAddress = new Address();
            testAddress.setStreet("123 Main Street");
            testAddress.setCity("Test City");
            testAddress.setZip(12345l);
            testAddress.setCountry("Test Country");
            
			List<Address> testAddressList = new ArrayList<>();
			testAddressList.add(testAddress);
			testPartner.setPartnerAddress(testAddressList);
			partnerService.addPartner(testPartner);

			Product testProduct = new Product();
			testProduct.setProductName("Test Product");
			testProduct.setProductPartnerID(testPartner.getPartnerID());
			testProduct.setProductSKU("TESTSKU123");
			testProduct.setProductPrice(9.99);
			testProduct.setProductQuantity(100);
			productService.addProduct(testProduct);

    	}

}