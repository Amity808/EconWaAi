package org.ecom.econwaai.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UnitConversion(
        UUID id,
        UUID productId,
        String localTradeUnit,
        BigDecimal multiplier,
        String marketName,
        UUID supplierOrgId,
        Instant createdAt
) implements Serializable {

    public static UnitConversion create(
            UUID productId,
            String localTradeUnit,
            BigDecimal multiplier,
            String marketName,
            UUID supplierOrgId
    ) {
        return new UnitConversion(
                UUID.randomUUID(),
                productId,
                localTradeUnit,
                multiplier,
                marketName,
                supplierOrgId,
                Instant.now()
        );
    }
}
