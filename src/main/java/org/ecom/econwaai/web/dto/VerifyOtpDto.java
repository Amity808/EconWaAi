package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpDto(
        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit numeric code")
        String otpCode
) {
}
