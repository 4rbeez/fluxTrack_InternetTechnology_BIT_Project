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
		// ---------- Partner 1: Wylaade GmbH ----------
		Partner wylaade = new Partner();
		wylaade.setPartnerName("Wylaade GmbH");
		wylaade.setPartnerEmail("info@wylaade.ch");
		wylaade.setPartnerPhone("+41 77 509 66 07");
	
		Address wylaadeAddress = new Address();
		wylaadeAddress.setName("Wylaade GmbH");
		wylaadeAddress.setStreet("Hauptstrasse");
		wylaadeAddress.setNumber("31");
		wylaadeAddress.setCity("Oberwil");
		wylaadeAddress.setZip(4104L);
		wylaadeAddress.setCountry("Switzerland");
	
		List<Address> wylaadeAddresses = new ArrayList<>();
		wylaadeAddresses.add(wylaadeAddress);
		wylaade.setPartnerAddress(wylaadeAddresses);
		partnerService.addPartner(wylaade);
		Long wylaadeId = wylaade.getPartnerID();
	
		productService.addProduct(makeProduct("00150", "Badaceli 2016 DOC Priorat",     27.50, 32, wylaadeId));
		productService.addProduct(makeProduct("00083", "Baselbieter Kerner 2022",        22.50,  0, wylaadeId));
		productService.addProduct(makeProduct("00301", "Palmeri Blu 2014",               39.50, 10, wylaadeId));
		productService.addProduct(makeProduct("00033", "Balatoni C-Cuvée 2019",          27.50, 16, wylaadeId));
		productService.addProduct(makeProduct("00014", "Zambartas Maratheftiko 2019",    29.50, 22, wylaadeId));
	
		// ---------- Partner 2: Drachehöhli GmbH ----------
		Partner drachehöhli = new Partner();
		drachehöhli.setPartnerName("Drachehöhli GmbH");
		drachehöhli.setPartnerEmail("info@drachehöhli.ch");
		drachehöhli.setPartnerPhone("+41 61 401 44 44");
	
		Address drachehöhliAddress = new Address();
		drachehöhliAddress.setName("Drachehöhli GmbH");
		drachehöhliAddress.setStreet("Passage");
		drachehöhliAddress.setNumber("6");
		drachehöhliAddress.setCity("Oberwil");
		drachehöhliAddress.setZip(4104L);
		drachehöhliAddress.setCountry("Switzerland");
	
		List<Address> drachehöhliAddresses = new ArrayList<>();
		drachehöhliAddresses.add(drachehöhliAddress);
		drachehöhli.setPartnerAddress(drachehöhliAddresses);
		partnerService.addPartner(drachehöhli);
		Long drachehöhliId = drachehöhli.getPartnerID();
	
		productService.addProduct(makeProduct("00501", "Drachenbluet IPA 6-Pack",        24.90, 48, drachehöhliId));
		productService.addProduct(makeProduct("00502", "Drachenfeuer Whisky 12yr",      129.00,  4, drachehöhliId));
		productService.addProduct(makeProduct("00503", "Höhlenbier Stout",              18.50,  0, drachehöhliId));
		productService.addProduct(makeProduct("00504", "Drachenhonig Met",               34.00, 12, drachehöhliId));
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