package org.ecom.econwaai.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record ProductSynonym(
        UUID id,
        UUID productId,
        String synonym,
        String languageOrDialect,
        Instant createdAt
) implements Serializable {

    public static ProductSynonym create(UUID productId, String synonym, String languageOrDialect) {
        return new ProductSynonym(
                UUID.randomUUID(),
                productId,
                synonym,
                languageOrDialect != null ? languageOrDialect : "en_NG",
                Instant.now()
        );
    }
}
