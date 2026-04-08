package com.raeden.ors_to_do.modules.dependencies.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WindowsStartupManager {
    private static final String APP_NAME = "TaskTracker_AutoStart.vbs";

    public static void setStartupEnabled(boolean enable) {
        try {
            if (!System.getProperty("os.name").toLowerCase().contains("win")) return;

            String startupFolder = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\";
            File vbsFile = new File(startupFolder + APP_NAME);

            if (enable) {
                String path = getApplicationPath();
                createVbsScript(vbsFile, path);
                System.out.println("Startup VBS created at: " + vbsFile.getAbsolutePath());
            } else {
                if (vbsFile.exists()) {
                    vbsFile.delete();
                    System.out.println("Startup VBS removed.");
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to modify Windows startup: " + e.getMessage());
        }
    }

    private static void createVbsScript(File vbsFile, String exePath) throws IOException {
        String script = "Set WshShell = CreateObject(\"WScript.Shell\")\n" +
                "WshShell.Run \"\"\"" + exePath + "\"\"\", 0, False";

        try (FileWriter writer = new FileWriter(vbsFile)) {
            writer.write(script);
        }
    }

    private static String getApplicationPath() {
        try {
            String path = new File(WindowsStartupManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            if (path.endsWith(".jar") || path.endsWith(".exe")) {
                return path;
            } else {
                return System.getProperty("user.dir") + File.separator + "Task-Tracker.exe";
            }
        } catch (Exception e) {
            return System.getProperty("user.dir") + File.separator + "Task-Tracker.exe";
        }
    }
}