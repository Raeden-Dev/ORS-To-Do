package com.raeden.ors_to_do.modules.dependencies.services;

import java.io.File;

public class WindowsStartupManager {
    private static final String APP_NAME = "ORSTaskTracker";

    public static void setStartupEnabled(boolean enable) {
        try {
            // Ensure this only runs on Windows systems
            if (!System.getProperty("os.name").toLowerCase().contains("win")) return;

            if (enable) {
                String path = getApplicationPath();
                // Format the command depending on if it's a jar or an exe
                String execCmd = path.endsWith(".jar") ? "javaw -jar \"" + path + "\"" : "\"" + path + "\"";

                String[] command = {
                        "reg", "add", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                        "/v", APP_NAME,
                        "/t", "REG_SZ",
                        "/d", execCmd,
                        "/f"
                };
                Runtime.getRuntime().exec(command);
            } else {
                String[] command = {
                        "reg", "delete", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                        "/v", APP_NAME,
                        "/f"
                };
                Runtime.getRuntime().exec(command);
            }
        } catch (Exception e) {
            System.out.println("Failed to modify registry for Windows startup: " + e.getMessage());
        }
    }

    private static String getApplicationPath() {
        try {
            String path = new File(WindowsStartupManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            if (path.endsWith(".jar") || path.endsWith(".exe")) {
                return path;
            } else {
                // Fallback for IDE testing / un-packaged environments
                return System.getProperty("user.dir") + File.separator + "Task-Tracker.exe";
            }
        } catch (Exception e) {
            return System.getProperty("user.dir") + File.separator + "Task-Tracker.exe";
        }
    }
}