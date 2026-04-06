package com.raeden.ors_to_do.modules.dependencies.ui.utils;

import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.File;
import java.net.URI;

public class TaskLinkUtil {
    public static void openActionPath(String path) {
        if (path == null || path.isEmpty()) return;
        new Thread(() -> {
            try {
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    java.awt.Desktop.getDesktop().browse(new URI(path));
                } else {
                    File file = new File(path);
                    if (file.exists()) java.awt.Desktop.getDesktop().open(file);
                    else Runtime.getRuntime().exec(path);
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open path: \n" + path);
                    alert.setHeaderText("Execution Error");
                    TaskDialogs.styleDialog(alert);
                    alert.show();
                });
            }
        }).start();
    }
}