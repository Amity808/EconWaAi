package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CacVerificationRequestDto(
        @NotBlank(message = "CAC Registration Number is required")
        @Pattern(regexp = "^(RC|BN|IT|LLP)?[0-9]{5,10}$", message = "Invalid CAC number format (e.g. RC1234567 or BN987654)")
        String cacNumber,

        @NotBlank(message = "Registered business or legal company name is required")
        String companyName
) {
}
