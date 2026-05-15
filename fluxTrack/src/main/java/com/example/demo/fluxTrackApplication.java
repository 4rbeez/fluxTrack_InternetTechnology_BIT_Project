package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.business.PartnerService;
import com.example.demo.business.ProductService;
import com.example.demo.data.domain.Address;
import com.example.demo.data.domain.Partner;
import com.example.demo.data.domain.Product;
import com.example.demo.data.repository.PartnerRepository;
import com.example.demo.data.repository.ProductRepository;

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
		// Seed Wylaade GmbH partner
		Partner wylaade = new Partner();
		wylaade.setPartnerName("Wylaade GmbH");
		wylaade.setPartnerEmail("info@wylaade.ch");
		wylaade.setPartnerPhone("+41 61 123 45 67");

		Address wylaadeAddress = new Address();
		wylaadeAddress.setName("Wylaade GmbH");
		wylaadeAddress.setStreet("Hauptstrasse");
		wylaadeAddress.setNumber("10a");
		wylaadeAddress.setCity("Oberwil");
		wylaadeAddress.setZip(4104L);
		wylaadeAddress.setCountry("Switzerland");

		List<Address> addresses = new ArrayList<>();
		addresses.add(wylaadeAddress);
		wylaade.setPartnerAddress(addresses);
		partnerService.addPartner(wylaade);

		Long pid = wylaade.getPartnerID();

		// Seed products from the Figma mockup
		productService.addProduct(makeProduct("00150", "Badaceli 2016 DOC Priorat",     27.50, 32, pid));
		productService.addProduct(makeProduct("00083", "Baselbieter Kerner 2022",        22.50,  0, pid));
		productService.addProduct(makeProduct("00301", "Palmeri Blu 2014",               39.50, 10, pid));
		productService.addProduct(makeProduct("00033", "Balatoni C-Cuvée 2019",          27.50, 16, pid));
		productService.addProduct(makeProduct("00014", "Zambartas Maratheftiko 2019",    29.50, 22, pid));
	}

	private static Product makeProduct(String sku, String name, double price, int qty, Long partnerId) {
		Product p = new Product();
		p.setProductSKU(sku);
		p.setProductName(name);
		p.setProductPrice(price);
		p.setProductQuantity(qty);
		p.setProductPartnerID(partnerId);
		return p;
	}

}