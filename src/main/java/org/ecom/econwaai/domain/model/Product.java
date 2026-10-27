package org.ecom.econwaai.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record Product(
        UUID id,
        String canonicalName,
        String category,
        String standardUnit,
        String description,
        boolean isActive,
        Instant createdAt
) implements Serializable {

    public static Product create(String canonicalName, String category, String standardUnit, String description) {
        return new Product(
                UUID.randomUUID(),
                canonicalName,
                category,
                standardUnit,
                description,
                true,
                Instant.now()
        );
    }
}
