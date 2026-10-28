package org.ecom.econwaai.infrastructure.sms;

/**
 * Service contract for sending transactional SMS notifications and OTP codes.
 */
public interface SmsService {

    /**
     * Sends an authentication OTP to the given phone number.
     *
     * @param phoneNumber Normalized destination phone number (e.g., +2348012345678)
     * @param otpCode 6-digit numeric OTP code
     * @return true if successfully dispatched
     */
    boolean sendOtp(String phoneNumber, String otpCode);
}
