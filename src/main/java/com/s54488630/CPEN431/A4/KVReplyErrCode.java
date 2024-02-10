package com.s54488630.CPEN431.A4;

public enum KVReplyErrCode {
    SUCCESS(0x00, "Operation is successful."),
    NON_EXISTENT_KEY(0x01, "Non-existent key requested in a get or delete operation."),
    OUT_OF_SPACE(0x02, "Out-of-space. Returned when there is no space left to store data for an additional PUT."),
    SYSTEM_OVERLOAD(0x03, "Temporary system overload. The system is operational but refuses the operation due to temporary overload."),
    INTERNAL_FAILURE(0x04, "Internal KVStore failure - a catch-all for all other situations where the KVStore cannot recover."),
    UNRECOGNIZED_COMMAND(0x05, "Unrecognized command."),
    INVALID_KEY(0x06, "Invalid key: the key length is invalid (e.g., greater than the maximum allowed length)."),
    INVALID_VALUE(0x07, "Invalid value: the value length is invalid (e.g., greater than the maximum allowed length).");

    private final int code;
    private final String description;

    KVReplyErrCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
