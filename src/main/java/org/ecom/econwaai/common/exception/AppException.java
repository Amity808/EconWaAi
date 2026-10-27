package org.ecom.econwaai.common.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Base domain exception envelope for all business, validation, and platform errors in EconWaAi.
 */
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;
    private final Instant timestamp;

    public AppException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), Collections.emptyMap(), null);
    }

    public AppException(ErrorCode errorCode, String message) {
        this(errorCode, message, Collections.emptyMap(), null);
    }

    public AppException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, Collections.emptyMap(), cause);
    }

    public AppException(ErrorCode errorCode, String message, Map<String, Object> details) {
        this(errorCode, message, details, null);
    }

    public AppException(ErrorCode errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode cannot be null");
        this.details = details != null ? Collections.unmodifiableMap(details) : Collections.emptyMap();
        this.timestamp = Instant.now();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
