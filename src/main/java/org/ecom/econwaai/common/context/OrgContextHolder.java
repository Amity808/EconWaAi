package org.ecom.econwaai.common.context;

import java.util.Optional;
import java.util.UUID;

/**
 * Thread-local context accessor for tenant and authentication metadata.
 * Fully compatible with Java 21 Virtual Threads and standard request thread pools.
 */
public final class OrgContextHolder {

    private static final ThreadLocal<OrgContext> CONTEXT = new ThreadLocal<>();

    private OrgContextHolder() {
    }

    public static void set(OrgContext context) {
        if (context == null) {
            clear();
        } else {
            CONTEXT.set(context);
        }
    }

    public static Optional<OrgContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static Optional<UUID> getOrgId() {
        return get().map(OrgContext::orgId);
    }

    public static Optional<UUID> getUserId() {
        return get().map(OrgContext::userId);
    }

    public static Optional<String> getCorrelationId() {
        return get().map(OrgContext::correlationId);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
