package org.ecom.econwaai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LivenessCallbackDto(
        @NotBlank(message = "Provider reference is required")
        String providerReference,

        @NotBlank(message = "Liveness status is required (e.g. PASSED, FAILED)")
        String status,

        @NotNull(message = "Liveness confidence score is required")
        BigDecimal livenessScore,

        String failureReason
) {
}
