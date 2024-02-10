package com.s54488630.CPEN431.A4;

import ca.NetSysLab.ProtocolBuffers.KeyValueResponse.KVResponse;
import com.google.protobuf.ByteString;

import java.security.Key;
import java.util.concurrent.ConcurrentHashMap;

public class KVStore {
    private final ConcurrentHashMap<ByteString, ValueVersionPair> store;
    private final int storeCap;
    private final int maxKeyLength = 32;
    private final int maxValueLength = 10000;
    public KVStore(int storeCap) {
        this.storeCap = storeCap;
        this.store = new ConcurrentHashMap<>(storeCap);
    }

    public KVResponse put(ByteString key, ByteString value, int version) {
        if (key.toByteArray().length > maxKeyLength) {
            return Utils.ConstructResponseWithErrCode(KVReplyErrCode.INVALID_KEY.getCode());
        }
        if (value.toByteArray().length > maxValueLength) {
            return Utils.ConstructResponseWithErrCode(KVReplyErrCode.INVALID_VALUE.getCode());
        }
        if (Utils.isMemoryUsageHigh(0.8)) {
            return Utils.ConstructResponseWithErrCode(KVReplyErrCode.OUT_OF_SPACE.getCode());

        }

        store.put(key, new ValueVersionPair(value, version));
        return KVResponse.newBuilder()
                .setErrCode(KVReplyErrCode.SUCCESS.getCode())
                .build();
    }

    public KVResponse get(ByteString key) {
        if (key.toByteArray().length > maxKeyLength) {
            return Utils.ConstructResponseWithErrCode(KVReplyErrCode.INVALID_KEY.getCode());
        }
        if (store.containsKey(key)) {
            ValueVersionPair pair = store.get(key);
              return KVResponse.newBuilder()
                    .setErrCode(KVReplyErrCode.SUCCESS.getCode())
                    .setValue(pair.getValue())
                    .setVersion(pair.getVersion())
                    .build();
        } else {
            return Utils.ConstructResponseWithErrCode(KVReplyErrCode.NON_EXISTENT_KEY.getCode());
        }
    }

    public KVResponse remove(ByteString key) {
        if (key.toByteArray().length > maxKeyLength) {
            return Utils.ConstructResponseWithErrCode(KVReplyErrCode.INVALID_KEY.getCode());
        }
        if (store.containsKey(key)) {
            store.remove(key);
            return Utils.ConstructResponseWithErrCode(KVReplyErrCode.SUCCESS.getCode());
        } else {
            return Utils.ConstructResponseWithErrCode(KVReplyErrCode.NON_EXISTENT_KEY.getCode());
        }
    }

    public KVResponse clear() {
       store.clear();
       return Utils.ConstructResponseWithErrCode(KVReplyErrCode.SUCCESS.getCode());
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }

    public boolean isFull() {
        return store.size() == storeCap;
    }

}
