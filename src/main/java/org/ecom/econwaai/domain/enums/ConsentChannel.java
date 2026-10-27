package org.ecom.econwaai.domain.enums;

public enum ConsentChannel {
    WHATSAPP_OTP("whatsapp_otp"),
    WEB_SIGNATURE("web_signature");

    private final String dbValue;

    ConsentChannel(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static ConsentChannel fromDbValue(String value) {
        for (ConsentChannel channel : values()) {
            if (channel.dbValue.equalsIgnoreCase(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown ConsentChannel: " + value);
    }
}
