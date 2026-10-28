package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;

public record WebviewUrlRequestDto(
        @NotBlank(message = "Verification flow type is required (e.g. LIVENESS, NIN, INFORMAL)")
        String verificationType,

        String returnUrl
) {
}
