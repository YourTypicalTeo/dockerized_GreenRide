package com.greenride.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Κλάση ρυθμίσεων (Configuration) για τον REST Client.
 * Εδώ ορίζουμε Beans που θα χρησιμοποιηθούν από το Spring Context.
 */
@Configuration
public class RestClientConfig {

    /**
     * Δημιουργεί ένα Bean του RestTemplate.
     * Το RestTemplate είναι ένα εργαλείο του Spring για την εκτέλεση HTTP αιτημάτων
     * σε εξωτερικά API (π.χ. για την αποστολή SMS μέσω του NocSmsAdapter).
     * * @return Ένα νέο αντικείμενο RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
