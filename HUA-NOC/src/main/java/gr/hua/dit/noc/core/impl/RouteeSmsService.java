package gr.hua.dit.noc.core.impl;

import gr.hua.dit.noc.config.RouteeProperties;
import gr.hua.dit.noc.core.SmsService;
import gr.hua.dit.noc.core.model.SendSmsRequest;
import gr.hua.dit.noc.core.model.SendSmsResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Service
public class RouteeSmsService implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteeSmsService.class);

    private static final String AUTHENTICATION_URL = "https://auth.routee.net/oauth/token";
    private static final String SMS_URL = "https://connect.routee.net/sms";

    private final RestTemplate restTemplate;
    private final RouteeProperties routeeProperties;

    public RouteeSmsService(final RestTemplate restTemplate, final RouteeProperties routeeProperties) {
        // IMPROVEMENT: Cleaner null checks
        this.restTemplate = Objects.requireNonNull(restTemplate, "RestTemplate must not be null");
        this.routeeProperties = Objects.requireNonNull(routeeProperties, "RouteeProperties must not be null");
    }

    @Cacheable("routeeAccessToken")
    public String getAccessToken() {
        LOGGER.info("Requesting Routee Access Token");

        try {
            final String credentials = this.routeeProperties.getAppId() + ":" + this.routeeProperties.getAppSecret();
            final String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "Basic " + encoded);
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            final HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", httpHeaders);

            // IMPROVEMENT: specific type <Map<String, Object>> instead of raw Map
            final ResponseEntity<Map> response =
                    this.restTemplate.exchange(AUTHENTICATION_URL, HttpMethod.POST, request, Map.class);

            // IMPROVEMENT: Null safety check
            if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
                LOGGER.error("Routee returned an empty body or missing access_token");
                return null;
            }

            return (String) response.getBody().get("access_token");

        } catch (HttpClientErrorException e) {
            // IMPROVEMENT: Catch 401/403 errors (Bad credentials)
            LOGGER.error("Routee Authentication Failed (4xx): {}", e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            // IMPROVEMENT: Catch Network errors (Downstream service down)
            LOGGER.error("Routee Network Error: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting Routee token", e);
            return null;
        }
    }

    @Override
    public SendSmsResult send(@Valid final SendSmsRequest sendSmsRequest) {
        // IMPROVEMENT: Cleaner validation
        if (sendSmsRequest == null) return new SendSmsResult(false);

        final String e164 = sendSmsRequest.e164();
        final String content = sendSmsRequest.content();

        if (e164 == null || e164.isBlank() || content == null || content.isBlank()) {
            LOGGER.warn("Attempted to send SMS with empty number or content");
            return new SendSmsResult(false);
        }

        // Authenticate
        final String token = this.getAccessToken();

        // IMPROVEMENT: Handle the error Don't crash if token is null
        if (token == null) {
            LOGGER.error("Cannot send SMS: Authentication token unavailable.");
            return new SendSmsResult(false);
        }

        try {
            // Headers
            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "Bearer " + token);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            // Payload
            final Map<String, Object> body = Map.of(
                    "body", content,
                    "to", e164,
                    "from", this.routeeProperties.getSender());

            // Request
            final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, httpHeaders);
            final ResponseEntity<String> response = this.restTemplate.postForEntity(SMS_URL, entity, String.class);

            LOGGER.info("Routee response code: {}", response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()) {
                LOGGER.error("SMS send to {} failed with status {}", e164, response.getStatusCode());
                return new SendSmsResult(false);
            }

            return new SendSmsResult(true);

        } catch (Exception e) {
            // IMPROVEMENT: Catch errors during the actual send (e.g. "Insufficient Balance" or "Bad Number")
            LOGGER.error("Exception while sending SMS to {}: {}", e164, e.getMessage());
            return new SendSmsResult(false);
        }
    }
}
