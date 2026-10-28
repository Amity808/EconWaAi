package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.ecom.econwaai.domain.enums.ConsentChannel;

public record ConsentRequestDto(
        @NotBlank(message = "Consent type is required (e.g. NDPA_IDENTITY_VERIFICATION)")
        String consentType,

        @NotBlank(message = "Legal consent text must be recorded under NDPA 2023")
        String consentText,

        @NotNull(message = "Granted channel is required (WHATSAPP_OTP or WEB_SIGNATURE)")
        ConsentChannel grantedChannel
) {
}
