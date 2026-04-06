package com.raeden.ors_to_do.dependencies.storage;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.TaskItem;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {

    private static final String APP_DIR = System.getenv("APPDATA") + File.separator + "TaskTracker";

    // --- NEW: JSON Files ---
    private static final String DATA_FILE = APP_DIR + File.separator + "tasks.json";
    private static final String STATS_FILE = APP_DIR + File.separator + "stats.json";

    // --- LEGACY: Old Binary Files for Migration ---
    private static final String LEGACY_DATA_FILE = APP_DIR + File.separator + "tasks.dat";
    private static final String LEGACY_STATS_FILE = APP_DIR + File.separator + "stats.dat";

    // --- GSON SETUP: Required to safely parse Java 8 Time objects ---
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME)))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, typeOfT, context) -> LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_TIME))
            .create();

    // --- JSON: Safe Save Engine with Rolling Backups ---
    private static void safeSaveJson(Object data, String baseFilename) {
        File directory = new File(APP_DIR);
        if (!directory.exists()) directory.mkdirs();

        File tempFile = new File(baseFilename + ".tmp");

        // 1. Write to temporary JSON file (UTF-8 to support emojis/icons)
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Failed to write temp JSON file for " + baseFilename + ": " + e.getMessage());
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
            System.out.println("Saved securely to JSON: " + baseFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- JSON: Safe Load Engine with Auto-Recovery ---
    private static <T> T safeLoadJson(String baseFilename, Type type) {
        String[] filesToTry = { baseFilename, baseFilename + ".bak1", baseFilename + ".bak2", baseFilename + ".bak3" };

        for (String path : filesToTry) {
            File f = new File(path);
            if (f.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
                    T obj = gson.fromJson(reader, type);
                    if (!path.equals(baseFilename)) {
                        System.out.println("⚠️ RECOVERED JSON FROM BACKUP: " + path);
                    }
                    return obj;
                } catch (Exception e) {
                    System.err.println("Corrupted JSON file detected: " + path + " - Attempting older backup...");
                }
            }
        }
        return null;
    }

    // --- LEGACY: Old Binary File Loader for Migration ---
    private static Object loadLegacyDat(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                return ois.readObject();
            } catch (Exception e) {
                System.err.println("Failed to load legacy .dat file: " + filename);
            }
        }
        return null;
    }

    // ==========================================
    // --- Public API ---
    // ==========================================

    public static void saveTasks(List<TaskItem> tasks) {
        safeSaveJson(tasks, DATA_FILE);
    }

    @SuppressWarnings("unchecked")
    public static List<TaskItem> loadTasks() {
        // 1. Try loading the modern JSON file
        Type type = new TypeToken<List<TaskItem>>(){}.getType();
        List<TaskItem> loaded = safeLoadJson(DATA_FILE, type);
        if (loaded != null) return loaded;

        // 2. MIGRATION: If JSON fails/doesn't exist, look for the old .dat file
        Object legacy = loadLegacyDat(LEGACY_DATA_FILE);
        if (legacy != null) {
            System.out.println("🔄 Migrating Tasks from Legacy .dat to .json format...");
            List<TaskItem> legacyTasks = (List<TaskItem>) legacy;
            saveTasks(legacyTasks); // Immediately secure it as JSON
            return legacyTasks;
        }

        return new ArrayList<>(); // Return empty list on total fresh install
    }

    public static void saveStats(AppStats stats) {
        safeSaveJson(stats, STATS_FILE);
    }

    public static AppStats loadStats() {
        // 1. Try loading the modern JSON file
        AppStats loaded = safeLoadJson(STATS_FILE, AppStats.class);
        if (loaded != null) return loaded;

        // 2. MIGRATION: If JSON fails/doesn't exist, look for the old .dat file
        Object legacy = loadLegacyDat(LEGACY_STATS_FILE);
        if (legacy != null) {
            System.out.println("🔄 Migrating AppStats from Legacy .dat to .json format...");
            AppStats legacyStats = (AppStats) legacy;
            saveStats(legacyStats); // Immediately secure it as JSON
            return legacyStats;
        }

        return new AppStats(); // Return fresh stats object on first run
    }
}