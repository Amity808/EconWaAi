package org.ecom.econwaai.common.exception;

/**
 * Standardized application error codes mapped to HTTP status codes.
 */
public enum ErrorCode {

    VALIDATION_ERROR(400, "VALIDATION_ERROR", "The provided payload failed validation constraints"),
    BAD_REQUEST(400, "BAD_REQUEST", "The request is malformed or invalid"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "Authentication credentials are missing or invalid"),
    FORBIDDEN(403, "FORBIDDEN", "The authenticated identity is not authorized for this action"),
    NOT_FOUND(404, "NOT_FOUND", "The requested resource was not found"),
    QUOTE_EXPIRED(410, "QUOTE_EXPIRED", "The quotation price lock guarantee has expired"),
    CONFLICT(409, "CONFLICT", "A duplicate or conflicting resource already exists"),
    BUSINESS_RULE_VIOLATION(422, "BUSINESS_RULE_VIOLATION", "The requested operation violates platform business rules"),
    RATE_LIMIT_EXCEEDED(429, "RATE_LIMIT_EXCEEDED", "Request rate limit exceeded. Please retry later"),
    EXTERNAL_SERVICE_ERROR(502, "EXTERNAL_SERVICE_ERROR", "An upstream partner service (Meta/MFB/Prembly) returned an error"),
    INTERNAL_ERROR(500, "INTERNAL_SERVER_ERROR", "An unexpected internal server error occurred");

    private final int httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
