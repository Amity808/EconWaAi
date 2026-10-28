package org.ecom.econwaai.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ConsentResponseDto(
        UUID id,
        UUID userId,
        UUID orgId,
        String consentType,
        String grantedChannel,
        String grantedIp,
        Instant grantedAt,
        String status
) {
}
