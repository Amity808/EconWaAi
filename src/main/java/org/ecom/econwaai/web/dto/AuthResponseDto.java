package org.ecom.econwaai.web.dto;

import java.util.UUID;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserSummaryDto user
) {
    public static AuthResponseDto of(
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            UserSummaryDto user
    ) {
        return new AuthResponseDto(accessToken, refreshToken, "Bearer", expiresInSeconds, user);
    }
}
