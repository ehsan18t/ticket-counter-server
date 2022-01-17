package dev.pages.ahsan.server;

import dev.pages.ahsan.user.User;
import dev.pages.ahsan.utils.Utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    public ServerSocket serverSocket;
    public static int clientCount = 0;
    public static HashMap<ClientHandler, String> clients = new HashMap<>();
    public static HashMap<String, User> data;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        String database = "database.ser";

        try {
            File file = new File(database);
            boolean isFileCreated = file.createNewFile();

            data = Utils.readHashMapFromFile(database);

            System.out.println("Server is waiting for client.");

            while (!serverSocket.isClosed()) {
                Socket sc =  serverSocket.accept();
                ClientHandler ch = new ClientHandler(sc);
                Thread t = new Thread(ch);
                t.start();
            }
        } catch (Exception e) {
            closeServer();
            e.printStackTrace();
        }
    }

    public void closeServer() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
