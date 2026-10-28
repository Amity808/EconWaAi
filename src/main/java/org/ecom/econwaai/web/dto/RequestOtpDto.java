package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RequestOtpDto(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Valid international or local phone number required (e.g., +2348012345678 or 08012345678)")
        String phoneNumber
) {
}
