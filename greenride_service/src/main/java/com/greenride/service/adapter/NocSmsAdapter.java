package com.greenride.service.adapter;

import com.greenride.dto.PhoneNumberValidationResult;
import com.greenride.service.port.SmsNotificationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NocSmsAdapter implements SmsNotificationPort {

    private final RestTemplate restTemplate;

    // Inject base URL (defaults to localhost for testing)
    @Value("${noc.service.url:http://localhost:8081/api/v1}")
    private String nocBaseUrl;

    // Inject API Key (Security Requirement)
    @Value("${noc.service.key:my-secret-api-token}")
    private String nocApiKey;

    @Autowired
    public NocSmsAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * POST External Call (Secured)
     * Στέλνει το SMS.
     */
    @Async
    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            // Body
            Map<String, String> payload = new HashMap<>();
            payload.put("e164", phoneNumber);
            payload.put("content", message);

            // Headers (Secured)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + nocApiKey);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            String url = nocBaseUrl + "/sms";

            // Εκτέλεση POST
            restTemplate.postForObject(url, request, String.class);

            System.out.println("SMS sent via NOC to " + phoneNumber);
        } catch (Exception e) {
            System.err.println("Failed to reach NOC service (SMS): " + e.getMessage());
        }
    }

    /**
     * GET External Call (Secured)
     * Ελέγχει αν το τηλέφωνο είναι έγκυρο.
     */
    @Override
    public boolean validatePhoneNumber(String phoneNumber) {
        try {
            String url = nocBaseUrl + "/phone-numbers/{phoneNumber}/validations";

            // Headers (Secured)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + nocApiKey);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            // Εκτέλεση GET
            ResponseEntity<PhoneNumberValidationResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    PhoneNumberValidationResult.class,
                    phoneNumber
            );

            if (response.getBody() != null) {
                return response.getBody().valid();
            }
        } catch (Exception e) {
            System.err.println("NOC Validation check failed: " + e.getMessage());
            // Αν το NOC είναι κάτω, επιστρέφουμε true για να μην μπλοκάρουμε τον χρήστη
            return true;
        }

        return false;
    }
}