package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.TaskItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {

    private static final String APP_DIR = System.getenv("APPDATA") + File.separator + "TaskTracker";
    private static final String DATA_FILE = APP_DIR + File.separator + "tasks.dat";
    private static final String STATS_FILE = APP_DIR + File.separator + "stats.dat"; // New file for stats

    // --- TASK MANAGEMENT ---

    public static void saveTasks(List<TaskItem> tasks) {
        File directory = new File(APP_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(tasks);
            System.out.println("Tasks saved successfully to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save tasks: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<TaskItem> loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new ArrayList<>(); // Return empty list on first run
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (List<TaskItem>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load tasks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // --- STATS MANAGEMENT ---

    public static void saveStats(AppStats stats) {
        File directory = new File(APP_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATS_FILE))) {
            oos.writeObject(stats);
            System.out.println("Stats saved successfully to " + STATS_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save stats: " + e.getMessage());
        }
    }

    public static AppStats loadStats() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return new AppStats(); // Return fresh stats object on first run
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STATS_FILE))) {
            return (AppStats) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load stats: " + e.getMessage());
            return new AppStats(); // Fallback to fresh object if loading fails
        }
    }
}