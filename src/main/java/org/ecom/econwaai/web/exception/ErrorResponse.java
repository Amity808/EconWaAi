package org.ecom.econwaai.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        int status,
        String correlationId,
        Instant timestamp,
        Map<String, Object> details
) {
    public static ErrorResponse of(String code, String message, int status, String correlationId, Map<String, Object> details) {
        return new ErrorResponse(code, message, status, correlationId, Instant.now(), details);
    }
}
