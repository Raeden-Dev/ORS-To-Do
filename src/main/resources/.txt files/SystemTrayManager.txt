package com.raeden.ors_to_do.modules.dependencies.services;

import com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class SystemTrayManager {
    private static java.awt.TrayIcon trayIcon;
    private static Stage mainStage;

    public static void setupSystemTray(Stage primaryStage, Runnable exitAction) {
        mainStage = primaryStage;
        if (!java.awt.SystemTray.isSupported()) return;

        java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
        java.awt.Image trayImage = null;

        // --- SAFE ICON LOADING ---
        try {
            java.net.URL imageURL = SystemTrayManager.class.getResource("/icon.png");
            if (imageURL != null) {
                trayImage = java.awt.Toolkit.getDefaultToolkit().getImage(imageURL);
            }
        } catch (Exception e) {
            System.out.println("Tray icon.png not found, falling back to default.");
        }

        // --- FALLBACK (If image is null or missing) ---
        if (trayImage == null) {
            java.awt.image.BufferedImage fallback = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = fallback.createGraphics();
            g2d.setColor(new java.awt.Color(86, 156, 214));
            g2d.fillOval(0, 0, 16, 16);
            g2d.dispose();
            trayImage = fallback;
        }

        trayIcon = new java.awt.TrayIcon(trayImage, "ORS Task Tracker");
        trayIcon.setImageAutoSize(true);

        trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    Platform.runLater(() -> {
                        primaryStage.show();
                        primaryStage.setIconified(false);
                        primaryStage.toFront();
                    });
                }
            }
        });

        java.awt.PopupMenu popup = new java.awt.PopupMenu();
        java.awt.MenuItem openItem = new java.awt.MenuItem("Open Task Tracker");
        openItem.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.setIconified(false);
            primaryStage.toFront();
        }));

        java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit Entirely");
        exitItem.addActionListener(e -> Platform.runLater(exitAction));

        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        try { tray.add(trayIcon); }
        catch (java.awt.AWTException e) { System.err.println("TrayIcon could not be added."); }
    }

    public static void pushNotification(String title, String message) {
        if (mainStage != null && !mainStage.isShowing()) {
            if (trayIcon != null) trayIcon.displayMessage(title, message, java.awt.TrayIcon.MessageType.INFO);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);

                // --- FIXED: Use global Dark Theme & Always-On-Top settings ---
                TaskDialogs.styleDialog(alert);

                alert.show();
            });
        }
    }
}