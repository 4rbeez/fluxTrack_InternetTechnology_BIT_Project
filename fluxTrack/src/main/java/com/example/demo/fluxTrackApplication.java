package com.example.demo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.business.OrderService;
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
	@Autowired
	private OrderService orderService;

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

		productService.addProduct(makeProduct("00501", "Pokémon Day 2026 Collection (DE+EN)",       25.90, 48, drachehöhliId));
		productService.addProduct(makeProduct("00502", "Azul",                                      36.00,  4, drachehöhliId));
		productService.addProduct(makeProduct("00503", "Magic - Lorwyn Eclipsed Draft Night (EN)",  89.90,  0, drachehöhliId));
		productService.addProduct(makeProduct("00504", "Naruto TCG: Special Pack EN",               32.90, 12, drachehöhliId));

		// ---------- Seed historical orders (UC 304) ----------
		List<Product> allProductsForSeed = productService.getAllProducts();
		LocalDateTime now = LocalDateTime.now();

		Map<String, Product> bySku = new HashMap<>();
		for (Product p : allProductsForSeed) {
			bySku.put(p.getProductSKU(), p);
		}

		// Wylaade orders
		seedOrderIfPresent(bySku, "00150", 2, now.minusDays(28).withHour(10).withMinute(15));
		seedOrderIfPresent(bySku, "00150", 1, now.minusDays(21).withHour(14).withMinute(40));
		seedOrderIfPresent(bySku, "00301", 1, now.minusDays(18).withHour(11).withMinute(5));
		seedOrderIfPresent(bySku, "00033", 2, now.minusDays(15).withHour(16).withMinute(20));
		seedOrderIfPresent(bySku, "00014", 3, now.minusDays(12).withHour(9).withMinute(50));
		seedOrderIfPresent(bySku, "00150", 1, now.minusDays(9).withHour(13).withMinute(10));
		seedOrderIfPresent(bySku, "00014", 1, now.minusDays(6).withHour(17).withMinute(30));
		seedOrderIfPresent(bySku, "00301", 2, now.minusDays(3).withHour(10).withMinute(45));

		// Drachehöhli orders
		seedOrderIfPresent(bySku, "00501", 6, now.minusDays(27).withHour(11).withMinute(0));
		seedOrderIfPresent(bySku, "00501", 12, now.minusDays(20).withHour(15).withMinute(20));
		seedOrderIfPresent(bySku, "00504", 2, now.minusDays(17).withHour(14).withMinute(5));
		seedOrderIfPresent(bySku, "00502", 1, now.minusDays(14).withHour(18).withMinute(0));
		seedOrderIfPresent(bySku, "00501", 6, now.minusDays(10).withHour(12).withMinute(30));
		seedOrderIfPresent(bySku, "00504", 4, now.minusDays(7).withHour(16).withMinute(15));
		seedOrderIfPresent(bySku, "00502", 1, now.minusDays(2).withHour(11).withMinute(45));
	}

	// Helper: insert a seeded order if the product with this SKU exists
	private void seedOrderIfPresent(Map<String, Product> bySku, String sku, int quantity, LocalDateTime when) {
		Product p = bySku.get(sku);
		if (p != null) {
			orderService.seedOrder(p, quantity, when);
		}
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