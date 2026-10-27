package org.ecom.econwaai.domain.model;

import org.ecom.econwaai.common.model.Kobo;
import org.ecom.econwaai.domain.enums.StockStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record SupplierCatalogItem(
        UUID id,
        UUID supplierOrgId,
        UUID productId,
        String customProductName,
        String tradeUnit,
        int moq,
        Kobo indicativePrice,
        StockStatus stockStatus,
        Integer availableQuantity,
        UUID pickupLocationId,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {

    public static SupplierCatalogItem create(
            UUID supplierOrgId,
            UUID productId,
            String customProductName,
            String tradeUnit,
            int moq,
            Kobo indicativePrice,
            StockStatus stockStatus,
            Integer availableQuantity,
            UUID pickupLocationId
    ) {
        Instant now = Instant.now();
        return new SupplierCatalogItem(
                UUID.randomUUID(),
                supplierOrgId,
                productId,
                customProductName,
                tradeUnit,
                Math.max(1, moq),
                indicativePrice,
                stockStatus != null ? stockStatus : StockStatus.IN_STOCK,
                availableQuantity,
                pickupLocationId,
                true,
                now,
                now
        );
    }
}
