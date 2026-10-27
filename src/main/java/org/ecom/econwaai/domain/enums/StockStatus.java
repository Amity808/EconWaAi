package org.ecom.econwaai.domain.enums;

public enum StockStatus {
    IN_STOCK("in_stock"),
    LOW_STOCK("low_stock"),
    OUT_OF_STOCK("out_of_stock");

    private final String dbValue;

    StockStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static StockStatus fromDbValue(String value) {
        for (StockStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown StockStatus: " + value);
    }
}
