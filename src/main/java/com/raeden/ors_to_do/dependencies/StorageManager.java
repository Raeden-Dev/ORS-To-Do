package com.raeden.ors_to_do.dependencies;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {

    private static final String APP_DIR = System.getenv("APPDATA") + File.separator + "TaskTracker";
    private static final String DATA_FILE = APP_DIR + File.separator + "tasks.dat";
    private static final String STATS_FILE = APP_DIR + File.separator + "stats.dat";

    // --- NEW: Safe Save Engine with Rolling Backups ---
    private static void safeSave(Object data, String baseFilename) {
        File directory = new File(APP_DIR);
        if (!directory.exists()) directory.mkdirs();

        File tempFile = new File(baseFilename + ".tmp");

        // 1. Write to temporary file to prevent corruption during writing
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Failed to write temp file for " + baseFilename + ": " + e.getMessage());
            return; // Abort save, keep old files intact
        }

        // 2. Rotate backups and commit
        try {
            for (int i = 2; i >= 1; i--) {
                File src = new File(baseFilename + ".bak" + i);
                File dest = new File(baseFilename + ".bak" + (i + 1));
                if (src.exists()) Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            File original = new File(baseFilename);
            File bak1 = new File(baseFilename + ".bak1");
            if (original.exists()) Files.move(original.toPath(), bak1.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Files.move(tempFile.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Saved securely with rolling backup: " + baseFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- NEW: Safe Load Engine with Auto-Recovery ---
    private static Object safeLoad(String baseFilename) {
        String[] filesToTry = { baseFilename, baseFilename + ".bak1", baseFilename + ".bak2", baseFilename + ".bak3" };

        for (String path : filesToTry) {
            File f = new File(path);
            if (f.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                    Object obj = ois.readObject();
                    if (!path.equals(baseFilename)) {
                        System.out.println("⚠️ RECOVERED DATA FROM BACKUP: " + path);
                    }
                    return obj;
                } catch (Exception e) {
                    System.err.println("Corrupted file detected: " + path + " - Attempting older backup...");
                }
            }
        }
        return null; // All failed or none exist
    }

    // --- Public API ---
    public static void saveTasks(List<TaskItem> tasks) {
        safeSave(tasks, DATA_FILE);
    }

    @SuppressWarnings("unchecked")
    public static List<TaskItem> loadTasks() {
        Object loaded = safeLoad(DATA_FILE);
        if (loaded != null) return (List<TaskItem>) loaded;
        return new ArrayList<>(); // Return empty list on first run or total failure
    }

    public static void saveStats(AppStats stats) {
        safeSave(stats, STATS_FILE);
    }

    public static AppStats loadStats() {
        Object loaded = safeLoad(STATS_FILE);
        if (loaded != null) return (AppStats) loaded;
        return new AppStats(); // Return fresh stats object on first run
    }
}