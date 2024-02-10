package com.s54488630.CPEN431.A4;

import ca.NetSysLab.ProtocolBuffers.KeyValueRequest;
import ca.NetSysLab.ProtocolBuffers.KeyValueResponse;
import ca.NetSysLab.ProtocolBuffers.Message.Msg;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.*;


public class KVServer {
    protected class ClientRequest {
       protected SocketAddress address;
       protected Msg msg;
       public ClientRequest(SocketAddress address, Msg msg) {
           this.address = address;
           this.msg = msg;
       }
    }
    private final int port;
    private volatile boolean isRunning = true;
    private final KVStore store;
    private final DatagramChannel channel;
    private final BlockingQueue<ClientRequest> requestQueue;
    private final ExecutorService processingThreadPool;
    private final Selector selector;
    private final int cacheCap;
//    private LRUCache<ByteString, KeyValueResponse.KVResponse> cache;
    private final int queueMaxCap = 10;
    private final ExpiringLRUCache<ByteString, KeyValueResponse.KVResponse> cache;
//    private volatile ExpiringLRUCache<ByteString, KVReplyErrCode> cache;
    public KVServer(int port, int storeCap, int cacheCap) throws IOException {
        this.port = port;
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.socket().bind(new InetSocketAddress(this.port));
        this.requestQueue = new LinkedBlockingQueue<>();
        this.store = new KVStore(storeCap);
        this.processingThreadPool = Executors.newSingleThreadExecutor();
        this.selector = Selector.open();
        this.cacheCap = cacheCap;
        this.cache = new ExpiringLRUCache<>(this.cacheCap, 1000);
    }

    public void Run() throws IOException {
        channel.register(selector, SelectionKey.OP_READ);
        ByteBuffer buffer = ByteBuffer.allocateDirect(50000);

        processingThreadPool.execute(this::processRequest);
        System.out.println("Server running on port " + this.port);
        while (isRunning) {
            if (selector.select(100) == 0) continue;
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isReadable()) {
                    buffer.clear();
                    SocketAddress clientAddress = channel.receive(buffer);
                    buffer.flip();
                    Msg msg = Utils.ParseMsg(buffer);
                    if (msg != null) {
                        if (Utils.MessageNotCorrupted(msg)) {
                            ByteString msgID = msg.getMessageID();
                            if (cache.containsKey(msgID)) {
                                Msg responseMsg = Utils.ConstructResponseMsg(msgID, cache.get(msgID));
                                channel.send(ByteBuffer.wrap(responseMsg.toByteArray()), clientAddress);
                            } else {
                                if (requestQueue.size() == queueMaxCap) {
                                    Msg responseMsg = Utils.ConstructResponseMsg(msgID, Utils.ConstructOverLoadResponse(10));
                                    channel.send(ByteBuffer.wrap(responseMsg.toByteArray()), clientAddress);
                                } else {
                                    requestQueue.add(new ClientRequest(clientAddress, msg));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void processRequest() {
        while (isRunning) {
            try {
//                Utils.printMemoryUsage();
                ClientRequest clientRequest = requestQueue.take();
                Msg requestMsg = clientRequest.msg;
                ByteString msgID = requestMsg.getMessageID();
                KeyValueResponse.KVResponse response = null;
                KeyValueRequest.KVRequest request = Utils.ParseRequest(requestMsg);
                int command = request.getCommand();

                ByteString key = request.getKey();
                ByteString value = request.getValue();
                int version = request.getVersion();

                if (command == KVRequestCommand.PUT.getCode()) {
                    response = store.put(key, value, version);
                } else if (command == KVRequestCommand.GET.getCode()) {
                    response = store.get(key);
                } else if (command == KVRequestCommand.REMOVE.getCode()) {
                    response = store.remove(key);
                } else if (command == KVRequestCommand.SHUTDOWN.getCode()) {
                    System.out.println("Shutdown command received. Shutting down the server!");
                    System.exit(0);
                } else if (command == KVRequestCommand.WIPE_OUT.getCode()) {
                    cache.clear();
                    response = store.clear();
                } else if (command == KVRequestCommand.IS_ALIVE.getCode()) {
                    response = KeyValueResponse.KVResponse.newBuilder()
                            .setErrCode(KVReplyErrCode.SUCCESS.getCode()).build();
                } else if (command == KVRequestCommand.GET_PID.getCode()) {
                    response = KeyValueResponse.KVResponse.newBuilder()
                            .setErrCode(KVReplyErrCode.SUCCESS.getCode())
                            .setPid((int)ProcessHandle.current().pid())
                            .build();
                } else if (command == KVRequestCommand.GET_MEMBERSHIP_COUNT.getCode()) {
                    response = KeyValueResponse.KVResponse.newBuilder()
                            .setErrCode(KVReplyErrCode.SUCCESS.getCode())
                            .setMembershipCount(1)
                            .build();
                } else {
                    response = KeyValueResponse.KVResponse.newBuilder()
                            .setErrCode(KVReplyErrCode.UNRECOGNIZED_COMMAND.getCode()).build();
                }
                if (response.getErrCode() != KVReplyErrCode.SYSTEM_OVERLOAD.getCode()) {
                    cache.put(msgID, response);
                }

                // send response back
                SocketAddress clientAddress = clientRequest.address;
                Msg responseMsg = Utils.ConstructResponseMsg(msgID, response);
                channel.send(ByteBuffer.wrap(responseMsg.toByteArray()), clientAddress);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                System.err.println(e.getMessage());
                break;
            }
        }
    }

    public void Stop() {
        shutdown();
    }

    private void shutdown() {
        isRunning = false;
        processingThreadPool.shutdown();
        try {
            this.channel.close();
            this.selector.close();
            this.cache.shutdown();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
