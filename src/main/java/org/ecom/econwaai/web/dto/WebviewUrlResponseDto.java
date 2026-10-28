package org.ecom.econwaai.web.dto;

import java.time.Instant;

public record WebviewUrlResponseDto(
        String webviewUrl,
        String token,
        Instant expiresAt,
        int ttlSeconds
) {
}
