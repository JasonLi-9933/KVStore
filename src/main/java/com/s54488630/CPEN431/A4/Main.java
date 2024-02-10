package com.s54488630.CPEN431.A4;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static KVServer server = null;
    public static void main(String[] args) {
        if (memoryCheckFailed()) {
            System.err.println("Memory of the runtime exceeds the requirement!");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            server = new KVServer(port, 20, 20);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (server != null) {
                    server.Stop();
                    System.out.println("Server resources cleaned up");
                }
            }));
            server.Run();
        }
        catch (Exception e) {
            System.err.println("Something is wrong!");
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.Stop();
            }
        }
    }

    private static boolean memoryCheckFailed() {
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024*1024);
        return maxMemory > 64;
    }
}