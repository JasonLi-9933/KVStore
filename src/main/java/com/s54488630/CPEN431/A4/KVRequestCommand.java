package com.s54488630.CPEN431.A4;

public enum KVRequestCommand {
    PUT(0x01, "This is a put operation."),
    GET(0x02, "This is a get operation."),
    REMOVE(0x03, "This is a remove operation."),
    SHUTDOWN(0x04, "Shuts-down the node (used for testing and management)."),
    WIPE_OUT(0x05, "Deletes all keys stored in the node (used for testing)"),
    IS_ALIVE(0x06, "Does nothing but replies with success if the node is alive."),
    GET_PID(0x07, "The node is expected to reply with the processID of the Java process"),
    GET_MEMBERSHIP_COUNT(0x08, "The node is expected to reply with the count of the currently active members based on your membership protocol. For now, expected to return 1");

    private final int code;
    private final String description;

    KVRequestCommand(int code, String description) {
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
