package com.example.healthease.relay;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RelayServer {
    private final int port;
    private final Map<String, Client> clients = new ConcurrentHashMap<>();

    public RelayServer(int port) { this.port = port; }

    public void start() throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Relay server listening on " + port);
            while (true) {
                Socket s = server.accept();
                new Thread(() -> handle(s)).start();
            }
        }
    }

    private void handle(Socket s) {
        String name = null;
        try (Socket socket = s;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("HELLO ")) {
                    name = line.substring(6).trim();
                    if (name.isEmpty()) { out.write("ERR badname\n"); out.flush(); continue; }
                    clients.put(name, new Client(name, socket, out));
                    System.out.println("Client joined: " + name);
                    out.write("OK\n"); out.flush();
                } else if (line.equals("LIST")) {
                    String list = String.join(",", clients.keySet());
                    out.write("USERS " + list + "\n"); out.flush();
                } else if (line.startsWith("MSG ")) {
                    int sp = line.indexOf(' ', 4);
                    if (sp > 4) {
                        String to = line.substring(4, sp);
                        String text = line.substring(sp + 1);
                        Client c = clients.get(to);
                        if (c != null) {
                            c.send("FROM " + name + " " + text);
                        } else {
                            out.write("ERR no-user\n"); out.flush();
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (name != null) {
                clients.remove(name);
                System.out.println("Client left: " + name);
            }
        }
    }

    private static class Client {
        @SuppressWarnings("unused")
        final String name; final Socket socket; final BufferedWriter out;
        Client(String n, Socket s, BufferedWriter o) { name=n; socket=s; out=o; }
        synchronized void send(String s) throws IOException { out.write(s + "\n"); out.flush(); }
    }

    public static void main(String[] args) throws Exception {
        int port = args.length>0? Integer.parseInt(args[0]) : 5050;
        new RelayServer(port).start();
    }
}
