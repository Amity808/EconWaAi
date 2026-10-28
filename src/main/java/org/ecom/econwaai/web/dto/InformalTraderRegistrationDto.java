package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InformalTraderRegistrationDto(
        @NotBlank(message = "Market name is required (e.g. Mile 12, Alaba, Bodija)")
        String marketName,

        @NotBlank(message = "Trade association name is required")
        String tradeAssociationName,

        String tradeAssociationRegNumber,

        @NotBlank(message = "Market leader (Iyaloja / Babaloja) name is required")
        String marketLeaderName,

        @NotBlank(message = "Market leader phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Valid phone number required")
        String marketLeaderPhone,

        String marketLeaderEndorsementLetterUrl,

        @NotBlank(message = "Guarantor full name is required")
        String guarantorFullName,

        @NotBlank(message = "Guarantor phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Valid guarantor phone number required")
        String guarantorPhone,

        @Pattern(regexp = "^[0-9]{11}$", message = "Guarantor NIN must be 11 digits if provided")
        String guarantorNin,

        @Pattern(regexp = "^[0-9]{11}$", message = "Guarantor BVN must be 11 digits if provided")
        String guarantorBvn
) {
}
