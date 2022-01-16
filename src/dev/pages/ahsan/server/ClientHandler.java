package dev.pages.ahsan.server;

import dev.pages.ahsan.user.User;
import dev.pages.ahsan.utils.Utils;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private int id;
    private Socket sc;
    private ObjectInputStream receiveObj;
    private ObjectOutputStream sendObj;

    public ClientHandler(Socket sc) {
        try {
            this.sc = sc;
            OutputStream oo = sc.getOutputStream();
            sendObj = new ObjectOutputStream(oo);

            InputStream inputStream = sc.getInputStream();
            receiveObj = new ObjectInputStream(inputStream);

            id = ++Server.clientCount;
            Server.clients.put(this, Server.clientCount + "");
            System.out.println(" - Connected with client " + id + "!");
        } catch (IOException e) {
            close(sc, receiveObj, sendObj);
        }
    }

    @Override
    public void run() {
        String request;
        try {
            while (sc.isConnected()) {
                // Receiving Request
                request = (String) receiveObj.readObject();
                User user = (User) receiveObj.readObject();
                task(user, request);
            }
        } catch (IOException | ClassNotFoundException e) {
            close(sc, receiveObj, sendObj);
            System.out.println(" - Disconnected client " + id + "!\n\n");
        }
    }

    void task(User user, String request) {
        try {
            System.out.println(" - Received request for " + request);

            // Handling Request
            switch (request) {
                case "registration" -> register(user);
                case "login" -> login(user);
            }
        } catch (Exception e) {
            close(sc, receiveObj, sendObj);
            e.printStackTrace();
        }
    }

    synchronized private void register(User user) throws IOException {
        System.out.println(" - Attempt to registration");
        if (!Server.data.containsKey(user.getEmail())) {
            Server.data.put(user.getEmail(), user);
            Utils.writeHashMapToFile(Server.data, "database.ser");
            sendObj.writeObject("SUCCESS");
            System.out.println(" - Registration Successful");
        } else {
            sendObj.writeObject("FAILED!");
            System.out.println(" - Registration failed! Duplicate email found!");
        }
    }

    private void login(User user) throws IOException {
        System.out.println(" - Attempt to Login");
        System.out.println(" - Passwords Hash: " + user.getPasswords());
        if (Server.data.containsKey(user.getEmail())) {
            if (Server.data.get(user.getEmail()).getPasswords().equals(user.getPasswords())) {
                System.out.println(" - login success");
                sendObj.writeObject("SUCCESS");
                System.out.println(" - sending user info");
                user = Server.data.get(user.getEmail());
                sendObj.writeObject(user);
                System.out.println(" - info send successfully!");
                return;
            }
        }
        System.out.println(" - Invalid credentials, login failed!!");
        sendObj.writeObject("FAILED!");
    }

    void removeClientHandler() {
        System.out.println(" - Finish Serving client " + Server.clients.get(this));
        Server.clients.remove(this);
    }

    void close(Socket sc, ObjectInputStream ois, ObjectOutputStream oos) {
        removeClientHandler();
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (sc != null) sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
