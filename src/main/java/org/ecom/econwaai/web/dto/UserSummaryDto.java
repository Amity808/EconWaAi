package org.ecom.econwaai.web.dto;

import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        UUID orgId,
        String phoneNumber,
        String fullName,
        String role,
        String orgType
) {
}
