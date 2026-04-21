package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class fluxTrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(fluxTrackApplication.class, args);
	}

	    @GetMapping("/welcome")
   		public String getWelcomeString() {
        return "Hello World";
    }


}