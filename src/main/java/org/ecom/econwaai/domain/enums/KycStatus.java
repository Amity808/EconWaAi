package org.ecom.econwaai.domain.enums;

public enum KycStatus {
    UNVERIFIED("unverified"),
    TIER_1_BASIC("tier_1_basic"),
    TIER_2_NIN_CAC("tier_2_nin_cac"),
    TIER_3_LIVENESS_INSPECTED("tier_3_liveness_inspected");

    private final String dbValue;

    KycStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static KycStatus fromDbValue(String value) {
        for (KycStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown KycStatus: " + value);
    }
}
