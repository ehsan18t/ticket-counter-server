package dev.pages.ehsan.main;

import dev.pages.ehsan.server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6611);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
