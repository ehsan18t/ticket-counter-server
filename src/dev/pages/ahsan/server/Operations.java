package dev.pages.ahsan.server;

import dev.pages.ahsan.user.User;
import dev.pages.ahsan.utils.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    synchronized public static void updateInfo(ObjectInputStream receiveObj) throws IOException, ClassNotFoundException {
        User user = (User) receiveObj.readObject();
        System.out.println(" - Attempt to update user info");
        Server.data.put(user.getEmail(), user);
        System.out.println(" - New Pass Hash: " + user.getPasswords());
        Utils.writeHashMapToFile(Server.data, "database.ser");
        System.out.println(" - Update Info Successful");
    }
}
