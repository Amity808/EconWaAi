package org.ecom.econwaai.security;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        // Use simulated SMS service and in-memory cache
        otpService = new OtpService(null, (phone, otp) -> true);
    }

    @Test
    @DisplayName("OTP request generates 6-digit code and normalizes phone number")
    void testRequestAndVerifyOtp() {
        String rawPhone = "08012345678";
        String normalized = otpService.normalizePhoneNumber(rawPhone);
        assertThat(normalized).isEqualTo("+2348012345678");

        String otp = otpService.requestOtp(normalized);
        assertThat(otp).hasSize(6).containsOnlyDigits();

        boolean verified = otpService.verifyOtp(normalized, otp);
        assertThat(verified).isTrue();

        // Second verification with same code fails because it was consumed
        assertThatThrownBy(() -> otpService.verifyOtp(normalized, otp))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("expired or was not requested");
    }

    @Test
    @DisplayName("Rate limiting rejects more than 3 requests within window")
    void testRateLimiting() {
        String phone = "+2348099887766";

        otpService.requestOtp(phone); // Attempt 1
        otpService.requestOtp(phone); // Attempt 2
        otpService.requestOtp(phone); // Attempt 3

        assertThatThrownBy(() -> otpService.requestOtp(phone))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException ae = (AppException) e;
                    assertThat(ae.getErrorCode()).isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED);
                });
    }

    @Test
    @DisplayName("Invalid verification code decrements attempts and rejects after 3 failed attempts")
    void testFailedVerifyAttempts() {
        String phone = "+2348055443322";
        otpService.requestOtp(phone);

        // Attempt 1 with wrong code
        assertThatThrownBy(() -> otpService.verifyOtp(phone, "000000"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("2 attempts remaining");

        // Attempt 2 with wrong code
        assertThatThrownBy(() -> otpService.verifyOtp(phone, "000000"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("1 attempts remaining");

        // Attempt 3 with wrong code
        assertThatThrownBy(() -> otpService.verifyOtp(phone, "000000"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("0 attempts remaining");

        // Attempt 4 triggers maximum attempts exceeded
        assertThatThrownBy(() -> otpService.verifyOtp(phone, "000000"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Maximum verification attempts exceeded");
    }
}
