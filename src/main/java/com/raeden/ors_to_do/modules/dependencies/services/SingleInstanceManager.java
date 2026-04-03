package com.raeden.ors_to_do.modules.dependencies.services;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleInstanceManager {
    private static final int INSTANCE_PORT = 44444;

    public static boolean checkAndWakeUp() {
        try {
            Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), INSTANCE_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("WAKE_UP");
            socket.close();
            System.out.println("App is already running in background. Waking up existing instance and closing this one.");
            return true;
        } catch (Exception e) {
            return false; // Connection refused, meaning no instance is running
        }
    }

    public static void startServer(Stage primaryStage) {
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(INSTANCE_PORT, 1, InetAddress.getByName("127.0.0.1"));
                while (true) {
                    Socket client = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String msg = in.readLine();
                    if ("WAKE_UP".equals(msg)) {
                        Platform.runLater(() -> {
                            if (primaryStage != null) {
                                primaryStage.show();
                                primaryStage.setIconified(false);
                                primaryStage.toFront();
                            }
                        });
                    }
                    client.close();
                }
            } catch (Exception e) {
                // Server socket failed to bind, likely because another app is using the port
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }
}