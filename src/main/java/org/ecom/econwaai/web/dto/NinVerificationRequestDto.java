package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NinVerificationRequestDto(
        @NotBlank(message = "National Identity Number (NIN) is required")
        @Pattern(regexp = "^[0-9]{11}$", message = "NIN must be exactly 11 digits")
        String nin,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Date of birth is required (YYYY-MM-DD)")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be YYYY-MM-DD")
        String dateOfBirth
) {
}
