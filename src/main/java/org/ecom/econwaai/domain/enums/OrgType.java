package org.ecom.econwaai.domain.enums;

public enum OrgType {
    BUYER("buyer"),
    SUPPLIER("supplier"),
    TRANSPORTER("transporter"),
    FINANCIAL_PARTNER("financial_partner"),
    PLATFORM_ADMIN("platform_admin");

    private final String dbValue;

    OrgType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static OrgType fromDbValue(String value) {
        for (OrgType type : values()) {
            if (type.dbValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown OrgType: " + value);
    }
}
