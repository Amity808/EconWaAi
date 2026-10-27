package org.ecom.econwaai.domain.enums;

public enum UserRole {
    OWNER("owner"),
    MANAGER("manager"),
    PROCUREMENT_OFFICER("procurement_officer"),
    SALES_REP("sales_rep"),
    DISPATCHER("dispatcher"),
    DRIVER("driver"),
    FINANCE_OFFICER("finance_officer"),
    OPS_AGENT("ops_agent");

    private final String dbValue;

    UserRole(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static UserRole fromDbValue(String value) {
        for (UserRole role : values()) {
            if (role.dbValue.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown UserRole: " + value);
    }
}
