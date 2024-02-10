package com.s54488630.CPEN431.A4;

import com.google.protobuf.ByteString;

public class ValueVersionPair {
    private ByteString value;
    private int version;

    public ValueVersionPair(ByteString value, int version) {
        this.value = value;
        this.version = version;
    }

    public ByteString getValue() {
        return value;
    }

    public void setValue(ByteString value) {
        this.value = value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
