package org.ecom.econwaai.security;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.infrastructure.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing 6-digit OTP generation, Redis 5-minute TTL caching,
 * and rate limiting (max 3 requests per phone number).
 */
@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    public static final Duration OTP_TTL = Duration.ofMinutes(5);
    public static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(15);
    public static final int MAX_REQUEST_ATTEMPTS = 3;
    public static final int MAX_VERIFY_ATTEMPTS = 3;

    private static final String OTP_PREFIX = "otp:code:";
    private static final String RATE_PREFIX = "otp:rate:";
    private static final String ATTEMPTS_PREFIX = "otp:attempts:";

    private final StringRedisTemplate redisTemplate;
    private final SmsService smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    // In-memory fallback for local environments or test slices when Redis is unavailable
    private final Map<String, CacheEntry> fallbackCache = new ConcurrentHashMap<>();

    private record CacheEntry(String value, long expiresAtMillis) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }

    public OtpService(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            SmsService smsService
    ) {
        this.redisTemplate = redisTemplate;
        this.smsService = smsService;
    }

    /**
     * Generates a 6-digit OTP, applies rate limiting, and dispatches via SMS.
     */
    public String requestOtp(String rawPhoneNumber) {
        String phone = normalizePhoneNumber(rawPhoneNumber);

        // Check rate limiting (max 3 requests per 15 minutes)
        checkAndIncrementRateLimit(phone);

        // Generate cryptographically secure 6-digit OTP
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        // Save OTP with 5-minute TTL
        saveValue(OTP_PREFIX + phone, otp, OTP_TTL);

        // Reset verification attempts
        deleteValue(ATTEMPTS_PREFIX + phone);

        // Dispatch via SMS
        smsService.sendOtp(phone, otp);

        log.info("Generated and dispatched OTP to {}", phone);
        return otp;
    }

    /**
     * Verifies the submitted OTP against Redis or fallback cache.
     */
    public boolean verifyOtp(String rawPhoneNumber, String submittedOtp) {
        String phone = normalizePhoneNumber(rawPhoneNumber);
        String savedOtp = getValue(OTP_PREFIX + phone);

        if (savedOtp == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Verification code has expired or was not requested");
        }

        // Check verification attempts
        String attemptsStr = getValue(ATTEMPTS_PREFIX + phone);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_VERIFY_ATTEMPTS) {
            deleteValue(OTP_PREFIX + phone);
            deleteValue(ATTEMPTS_PREFIX + phone);
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Maximum verification attempts exceeded. Please request a new code.");
        }

        if (!savedOtp.equals(submittedOtp.trim())) {
            int newAttempts = attempts + 1;
            saveValue(ATTEMPTS_PREFIX + phone, String.valueOf(newAttempts), OTP_TTL);
            int remaining = MAX_VERIFY_ATTEMPTS - newAttempts;
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid verification code. " + remaining + " attempts remaining.");
        }

        // Success: invalidate OTP and attempts
        deleteValue(OTP_PREFIX + phone);
        deleteValue(ATTEMPTS_PREFIX + phone);
        return true;
    }

    private void checkAndIncrementRateLimit(String phone) {
        String key = RATE_PREFIX + phone;
        String countStr = getValue(key);

        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        if (count >= MAX_REQUEST_ATTEMPTS) {
            throw new AppException(
                    ErrorCode.RATE_LIMIT_EXCEEDED,
                    "Too many OTP requests. Maximum " + MAX_REQUEST_ATTEMPTS + " requests allowed every 15 minutes."
            );
        }

        saveValue(key, String.valueOf(count + 1), RATE_LIMIT_WINDOW);
    }

    public String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Phone number is required");
        }
        String cleaned = phone.replaceAll("[^0-9+]", "");
        if (cleaned.startsWith("0")) {
            // Nigerian local format e.g. 08012345678 -> +2348012345678
            cleaned = "+234" + cleaned.substring(1);
        } else if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }
        return cleaned;
    }

    private void saveValue(String key, String value, Duration ttl) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, ttl);
                return;
            } catch (Exception e) {
                log.warn("Redis unavailable, using memory fallback for OTP key [{}]: {}", key, e.getMessage());
            }
        }
        fallbackCache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttl.toMillis()));
    }

    private String getValue(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.warn("Redis unavailable, checking memory fallback for OTP key [{}]: {}", key, e.getMessage());
            }
        }
        CacheEntry entry = fallbackCache.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                fallbackCache.remove(key);
                return null;
            }
            return entry.value();
        }
        return null;
    }

    private void deleteValue(String key) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis delete failed for key [{}]: {}", key, e.getMessage());
            }
        }
        fallbackCache.remove(key);
    }
}
