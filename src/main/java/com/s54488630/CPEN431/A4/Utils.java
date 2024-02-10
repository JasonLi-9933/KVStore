package com.s54488630.CPEN431.A4;

import ca.NetSysLab.ProtocolBuffers.KeyValueRequest;
import ca.NetSysLab.ProtocolBuffers.KeyValueResponse;
import ca.NetSysLab.ProtocolBuffers.Message;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Utils {
    static Message.Msg ParseMsg(ByteBuffer buffer) {
        try {
            byte[] dataBytes = new byte[buffer.remaining()];
            buffer.get(dataBytes);
            return Message.Msg.parseFrom(dataBytes);
        } catch (InvalidProtocolBufferException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    static KeyValueRequest.KVRequest ParseRequest(Message.Msg msg) {
        try {
            byte[] payload = msg.getPayload().toByteArray();
            return KeyValueRequest.KVRequest.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    static boolean MessageNotCorrupted(Message.Msg msg) {
        long receivedChecksum = msg.getCheckSum();
        byte[] msgIDBytes = msg.getMessageID().toByteArray();
        byte[] payloadBytes = msg.getPayload().toByteArray();
        CRC32 crc = new CRC32();
        crc.update(msgIDBytes);
        crc.update(payloadBytes);
        return crc.getValue() == receivedChecksum;
    }
    static long CalculateChecksum(byte[] msgID, byte[] payload) {
        CRC32 crc = new CRC32();
        crc.update(msgID);
        crc.update(payload);
        return crc.getValue();
    }

    static Message.Msg ConstructResponseMsg(ByteString msgID, KeyValueResponse.KVResponse response) {
        long responseChecksum = Utils.CalculateChecksum(msgID.toByteArray(), response.toByteArray());
        return Message.Msg.newBuilder()
                .setMessageID(msgID)
                .setPayload(response.toByteString())
                .setCheckSum(responseChecksum)
                .build();
    }

    static KeyValueResponse.KVResponse ConstructResponseWithErrCode(int errCode) {
        return KeyValueResponse.KVResponse.newBuilder()
                .setErrCode(errCode)
                .build();
    }
    static KeyValueResponse.KVResponse ConstructOverLoadResponse(int overtime) {
        return KeyValueResponse.KVResponse.newBuilder()
                .setErrCode(KVReplyErrCode.SYSTEM_OVERLOAD.getCode())
                .setOverloadWaitTime(overtime)
                .build();
    }

    static boolean isMemoryUsageHigh(double threshold) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // Max memory that can be used by JVM
        long usedMemory = runtime.totalMemory() - runtime.freeMemory(); // Currently used memory
        double usage = (double) usedMemory / maxMemory; // Calculate the usage percentage
        return usage > threshold;
    }

    static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // Max memory that can be used by JVM
        long usedMemory = runtime.totalMemory() - runtime.freeMemory(); // Currently used memory
        float usagePercent = (float) usedMemory / maxMemory * 100;
        float freePercent = (float) runtime.freeMemory() / maxMemory * 100;
        System.out.println("Max Mem: " + runtime.maxMemory()/(1024*1024));
        System.out.println("Free Mem: " + runtime.freeMemory()/(1024*1024));
        System.out.println("Used Mem: " + usedMemory/(1024*1024));
        System.out.println("Used Mem %: " + usagePercent + "%");
        System.out.println("Free Mem %: " + freePercent + "%");
        System.out.println("----");
    }

}
