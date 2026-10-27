package org.ecom.econwaai.common.context;

import java.io.Serializable;
import java.util.UUID;

/**
 * Tenant and identity context holder record for the current execution thread / request.
 */
public record OrgContext(
        UUID orgId,
        String orgType,
        UUID userId,
        String role,
        String correlationId
) implements Serializable {

    public static OrgContext anonymous(String correlationId) {
        return new OrgContext(null, "anonymous", null, "ANONYMOUS", correlationId);
    }
}
