package com.greenride.service.adapter;

import com.greenride.dto.PhoneNumberValidationResult;
import com.greenride.service.port.SmsNotificationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import added
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    // CHANGED: We inject the URL from application.properties
    // If no property is found, it defaults to localhost (for local testing)
    @Value("${noc.service.url:http://localhost:8081/api/v1}")
    private String nocBaseUrl;

    @Autowired
    public NocSmsAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("e164", phoneNumber);
            payload.put("content", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer my-secret-api-token");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            // CHANGED: Build the URL dynamically
            String url = nocBaseUrl + "/sms";

            restTemplate.postForObject(url, request, String.class);

            System.out.println("SMS sent via NOC to " + phoneNumber);
        } catch (Exception e) {
            System.err.println("Failed to reach NOC service: " + e.getMessage());
        }
    }

    @Override
    public boolean validatePhoneNumber(String phoneNumber) {
        try {
            // CHANGED: Use the dynamic base URL
            String url = nocBaseUrl + "/phone-numbers/{phoneNumber}/validations";

            ResponseEntity<PhoneNumberValidationResult> response =
                    restTemplate.getForEntity(url, PhoneNumberValidationResult.class, phoneNumber);

            if (response.getBody() != null) {
                return response.getBody().valid();
            }
        } catch (Exception e) {
            System.err.println("Validation check failed: " + e.getMessage());
            return false;
        }

        return false;
    }
}