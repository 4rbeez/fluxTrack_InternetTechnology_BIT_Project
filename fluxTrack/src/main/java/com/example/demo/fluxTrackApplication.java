package com.example.demo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.example.demo.business.SupportTicketService;
import com.example.demo.data.domain.Address;
import com.example.demo.data.domain.Partner;
import com.example.demo.data.domain.Product;
import com.example.demo.data.domain.TicketMessage;
import com.example.demo.data.domain.TicketState;
import com.example.demo.data.domain.TicketUrgency;

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
	@Autowired
	private SupportTicketService ticketService;

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

		seedOrderIfPresent(bySku, "00150", 2, now.minusDays(28).withHour(10).withMinute(15));
		seedOrderIfPresent(bySku, "00150", 1, now.minusDays(21).withHour(14).withMinute(40));
		seedOrderIfPresent(bySku, "00301", 1, now.minusDays(18).withHour(11).withMinute(5));
		seedOrderIfPresent(bySku, "00033", 2, now.minusDays(15).withHour(16).withMinute(20));
		seedOrderIfPresent(bySku, "00014", 3, now.minusDays(12).withHour(9).withMinute(50));
		seedOrderIfPresent(bySku, "00150", 1, now.minusDays(9).withHour(13).withMinute(10));
		seedOrderIfPresent(bySku, "00014", 1, now.minusDays(6).withHour(17).withMinute(30));
		seedOrderIfPresent(bySku, "00301", 2, now.minusDays(3).withHour(10).withMinute(45));

		seedOrderIfPresent(bySku, "00501", 6, now.minusDays(27).withHour(11).withMinute(0));
		seedOrderIfPresent(bySku, "00501", 12, now.minusDays(20).withHour(15).withMinute(20));
		seedOrderIfPresent(bySku, "00504", 2, now.minusDays(17).withHour(14).withMinute(5));
		seedOrderIfPresent(bySku, "00502", 1, now.minusDays(14).withHour(18).withMinute(0));
		seedOrderIfPresent(bySku, "00501", 6, now.minusDays(10).withHour(12).withMinute(30));
		seedOrderIfPresent(bySku, "00504", 4, now.minusDays(7).withHour(16).withMinute(15));
		seedOrderIfPresent(bySku, "00502", 1, now.minusDays(2).withHour(11).withMinute(45));

		// ---------- Seed support tickets (UC 107) ----------

		// 1. Wylaade — OPEN, no admin reply yet (1 message)
		ticketService.seedTicket(
			wylaadeId,
			"Cannot log in after password reset",
			TicketUrgency.HIGH,
			TicketState.OPEN,
			now.minusDays(2).withHour(9).withMinute(12),
			Arrays.asList(
				new TicketMessage("wylaade",
					"Hi, since I reset my password yesterday I can no longer log in. The error says \"invalid credentials\" but I'm sure the password is correct.",
					now.minusDays(2).withHour(9).withMinute(12))
			)
		);

		// 2. Wylaade — ANSWERED, admin has responded (2 messages)
		ticketService.seedTicket(
			wylaadeId,
			"Stock count for Palmeri Blu wrong after import",
			TicketUrgency.MEDIUM,
			TicketState.ANSWERED,
			now.minusDays(5).withHour(14).withMinute(30),
			Arrays.asList(
				new TicketMessage("wylaade",
					"After the bulk update yesterday the stock for Palmeri Blu 2014 shows 10 units but in our warehouse we have 25. Could you check what went wrong?",
					now.minusDays(5).withHour(14).withMinute(30)),
				new TicketMessage("admin",
					"Hello, thanks for reporting this. We've reviewed the import log and the issue was a mismatched SKU. You should now be able to adjust the stock manually to 25. Please confirm if this works for you.",
					now.minusDays(5).withHour(16).withMinute(45))
			)
		);

		// 3. Drachehöhli — RESOLVED (3 messages)
		ticketService.seedTicket(
			drachehöhliId,
			"How do I export a price list?",
			TicketUrgency.LOW,
			TicketState.RESOLVED,
			now.minusDays(10).withHour(11).withMinute(0),
			Arrays.asList(
				new TicketMessage("drachehoehli",
					"Hi team, is there a way to export the current price list as PDF or Excel? I'd like to share it with our store staff.",
					now.minusDays(10).withHour(11).withMinute(0)),
				new TicketMessage("admin",
					"Hi, the export feature is on our roadmap but not yet available. As a workaround you can use the partner overview page and copy-paste into your spreadsheet tool. Let me know if that works for now.",
					now.minusDays(10).withHour(13).withMinute(20)),
				new TicketMessage("drachehoehli",
					"Problem confirmed as resolved.",
					now.minusDays(9).withHour(8).withMinute(15))
			)
		);

		// 4. Drachehöhli — COMPLETED (long-closed)
		ticketService.seedTicket(
			drachehöhliId,
			"Welcome onboarding — account setup completed",
			TicketUrgency.LOW,
			TicketState.COMPLETED,
			now.minusDays(45).withHour(10).withMinute(0),
			Arrays.asList(
				new TicketMessage("drachehoehli",
					"First message to confirm our account is set up correctly. Everything looks fine on our side.",
					now.minusDays(45).withHour(10).withMinute(0)),
				new TicketMessage("admin",
					"Welcome to fluxed! Glad to have you onboard. If you run into anything, this is the right place to ask.",
					now.minusDays(45).withHour(11).withMinute(30)),
				new TicketMessage("drachehoehli",
					"Problem confirmed as resolved.",
					now.minusDays(44).withHour(9).withMinute(45)),
				new TicketMessage("admin",
					"Ticket closed as completed.",
					now.minusDays(40).withHour(14).withMinute(0))
			)
		);
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
