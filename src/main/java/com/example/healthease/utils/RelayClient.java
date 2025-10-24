package com.example.healthease.utils;

import java.io.*;
import java.net.Socket;
import java.util.function.BiConsumer;

public class RelayClient {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Thread readerThread;
    private volatile boolean connected = false;
    @SuppressWarnings("unused")
    private String username;

    private BiConsumer<String,String> onMessage; // (from, text)
    private Runnable onConnected;
    private Runnable onDisconnected;

    public void setOnMessage(BiConsumer<String,String> handler) { this.onMessage = handler; }
    public void setOnConnected(Runnable r) { this.onConnected = r; }
    public void setOnDisconnected(Runnable r) { this.onDisconnected = r; }

    public boolean isConnected() { return connected; }

    public void connect(String host, int port, String username) throws IOException {
        disconnect();
        this.username = username;
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        // Simple handshake
        sendRaw("HELLO " + username);
        connected = true;
        if (onConnected != null) onConnected.run();
        readerThread = new Thread(this::readLoop, "RelayClient-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                // Expected formats: FROM <sender> <text...>
                if (line.startsWith("FROM ")) {
                    int sp = line.indexOf(' ', 5);
                    if (sp > 5) {
                        String from = line.substring(5, sp);
                        String text = line.substring(sp + 1);
                        if (onMessage != null) onMessage.accept(from, text);
                    }
                }
                // Ignore other server messages for now
            }
        } catch (IOException ignored) {
        } finally {
            connected = false;
            if (onDisconnected != null) onDisconnected.run();
            try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        }
    }

    public void requestUserList() throws IOException {
        sendRaw("LIST");
    }

    public void sendMessage(String to, String text) throws IOException {
        sendRaw("MSG " + to + " " + text.replace('\n',' ').replace('\r',' '));
    }

    private synchronized void sendRaw(String s) throws IOException {
        if (out == null) throw new IOException("Not connected");
        out.write(s);
        out.write("\n");
        out.flush();
    }

    public synchronized void disconnect() {
        connected = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        socket = null; in = null; out = null;
    }
}
