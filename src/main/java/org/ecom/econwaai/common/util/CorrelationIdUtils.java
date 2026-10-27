package org.ecom.econwaai.common.util;

import java.util.UUID;

/**
 * Standard correlation ID constants and generator.
 */
public final class CorrelationIdUtils {

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdUtils() {
    }

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
