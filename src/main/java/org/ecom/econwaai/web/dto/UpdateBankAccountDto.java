package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBankAccountDto(
        @NotBlank(message = "Bank code / CBN code is required")
        @Pattern(regexp = "^[0-9]{3,6}$", message = "Bank code must be 3 to 6 digits")
        String bankCode,

        @NotBlank(message = "NUBAN account number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "NUBAN account number must be exactly 10 digits")
        String accountNumber,

        @NotBlank(message = "Account name is required")
        String accountName
) {
}
