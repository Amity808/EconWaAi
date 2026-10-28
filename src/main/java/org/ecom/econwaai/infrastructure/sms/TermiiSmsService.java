package org.ecom.econwaai.infrastructure.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Termii SMS Client implementing primary fallback transactional SMS delivery for Nigeria.
 */
@Service
public class TermiiSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(TermiiSmsService.class);

    private final String apiKey;
    private final String baseUrl;
    private final String senderId;
    private final RestClient restClient;

    public TermiiSmsService(
            @Value("${app.termii.api-key:}") String apiKey,
            @Value("${app.termii.base-url:https://api.ng.termii.com}") String baseUrl,
            @Value("${app.termii.sender-id:EconWa}") String senderId
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.senderId = senderId;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public boolean sendOtp(String phoneNumber, String otpCode) {
        String message = "Your EconWaAi verification code is: " + otpCode + ". Valid for 5 minutes. Do not share.";

        if (apiKey == null || apiKey.isBlank()) {
            // Development / Test mode logging
            log.info("[SMS-DEV-MODE] Dispatched OTP [{}] to [{}] via Termii simulator", otpCode, phoneNumber);
            return true;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "to", phoneNumber,
                    "from", senderId,
                    "sms", message,
                    "type", "plain",
                    "channel", "generic",
                    "api_key", apiKey
            );

            restClient.post()
                    .uri("/api/sms/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Termii OTP SMS sent successfully to {}", phoneNumber);
            return true;
        } catch (Exception e) {
            log.error("Failed to send Termii SMS to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
}
