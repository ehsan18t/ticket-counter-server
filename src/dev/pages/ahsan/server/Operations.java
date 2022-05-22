package dev.pages.ahsan.server;

import dev.pages.ahsan.main.Main;
import dev.pages.ahsan.user.Bus;
import dev.pages.ahsan.user.Ticket;
import dev.pages.ahsan.user.User;
import dev.pages.ahsan.utils.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Operations {
    synchronized public static void register(ObjectOutputStream sendObj, ObjectInputStream receiveObj) throws IOException, ClassNotFoundException {
        User user = (User) receiveObj.readObject();
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

    public static void login(ObjectOutputStream sendObj, ObjectInputStream receiveObj) throws IOException, ClassNotFoundException {
        User user = (User) receiveObj.readObject();
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

    public static void logout(ObjectOutputStream sendObj) throws IOException {
        sendObj.writeObject("Success");
    }

    synchronized public static void updateInfo(ObjectInputStream receiveObj) throws IOException, ClassNotFoundException {
        User user = (User) receiveObj.readObject();
        System.out.println(" - Attempt to update user info");
        Server.data.put(user.getEmail(), user);
        System.out.println(" - New Pass Hash: " + user.getPasswords());
        Utils.writeHashMapToFile(Server.data, "database.ser");
        System.out.println(" - Update Info Successful");
    }

    synchronized public static void addBus(ObjectInputStream receiveObj, String cmd) throws IOException, ClassNotFoundException {
        Bus bus = (Bus) receiveObj.readObject();
        Server.busData.put(bus, new HashMap<>());
        Utils.writeBusDataToFile(Server.busData, "busData.ser");
        refresh(cmd);
    }

    synchronized public static void overWrite(ObjectInputStream receiveObj, String cmd) throws IOException, ClassNotFoundException {
        Server.busData = (HashMap<Bus, HashMap<String, ArrayList<Ticket>>>) receiveObj.readObject();
        Utils.writeBusDataToFile(Server.busData, "busData.ser");
        refresh(cmd);
    }

    public static void getBusList(ObjectOutputStream sendObj) throws IOException {
        HashSet<Bus> buses = new HashSet<>();
        for (Map.Entry<Bus, HashMap<String, ArrayList<Ticket>>> entry: Server.busData.entrySet()) {
            buses.add(entry.getKey());
        }
        sendObj.writeObject(buses);
    }

    public static void getBusData(ObjectOutputStream sendObj) throws IOException {
        sendObj.writeObject(Server.busData);
    }

    synchronized public static void refresh(String cmd) {
        System.out.println(" - Start Refreshing...");
        try {
            for (Map.Entry<ClientHandler, String> entry: Server.clients.entrySet()) {
                System.out.println(" - Refresh Client " + entry.getValue());
                entry.getKey().sendObj.writeObject(cmd);
                entry.getKey().sendObj.writeObject(Server.busData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
