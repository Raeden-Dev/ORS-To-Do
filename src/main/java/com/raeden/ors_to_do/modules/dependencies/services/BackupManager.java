package com.raeden.ors_to_do.modules.dependencies.services;

import com.google.gson.*;
import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackupManager {

    // --- GSON SETUP: Required to safely parse Java 8 Time objects into JSON ---
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME)))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, typeOfT, context) -> LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_TIME))
            .create();

    /**
     * Exports ALL current application data to a JSON backup file.
     */
    public static void exportData(AppStats appStats, List<TaskItem> globalDatabase) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Full Backup");
        // FIXED: Switched extension to JSON
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Backup", "*.json"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                BackupBundle bundle = new BackupBundle(appStats, globalDatabase);
                // FIXED: Write via Gson to standard text file
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    gson.toJson(bundle, writer);
                }

                Alert success = new Alert(Alert.AlertType.INFORMATION, "Full backup exported successfully to:\n" + file.getAbsolutePath());
                success.setHeaderText("Export Successful");
                TaskDialogs.styleDialog(success);
                success.show();

            } catch (Exception ex) {
                ex.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR, "Failed to export data:\n" + ex.getMessage());
                error.setHeaderText("Export Error");
                TaskDialogs.styleDialog(error);
                error.show();
            }
        }
    }

    /**
     * Exports only specific slices of data to JSON based on the user's checkbox selections.
     */
    public static void exportCustomData(AppStats appStats, List<TaskItem> globalDatabase, Map<String, Boolean> exportFlags, List<String> selectedSectionIds) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Custom Backup");
        // FIXED: Switched extension to JSON
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Backup", "*.json"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                AppStats exportStats = new AppStats();

                // 1. Analytics & Stats
                if (exportFlags.getOrDefault("globalAnalytics", false)) {
                    exportStats.setGlobalScore(appStats.getGlobalScore());
                    exportStats.setCurrentStreak(appStats.getCurrentStreak());
                    exportStats.setHighestStreak(appStats.getHighestStreak());
                    exportStats.setLifetimeDeletedTasks(appStats.getLifetimeDeletedTasks());
                    exportStats.setLifetimePointsSpent(appStats.getLifetimePointsSpent());
                    exportStats.setRewardsClaimed(appStats.getRewardsClaimed());
                    exportStats.getHistoryLog().clear();
                    exportStats.getHistoryLog().putAll(appStats.getHistoryLog());
                    exportStats.getAdvancedHistoryLog().clear();
                    exportStats.getAdvancedHistoryLog().putAll(appStats.getAdvancedHistoryLog());
                }

                if (exportFlags.getOrDefault("currentStats", false)) {
                    exportStats.setCustomStats(new ArrayList<>(appStats.getCustomStats()));
                    exportStats.getLastStatGainDates().clear();
                    exportStats.getLastStatGainDates().putAll(appStats.getLastStatGainDates());
                }

                // 2. Settings & Configurations
                if (exportFlags.getOrDefault("dynamicSectionsConfig", false)) {
                    exportStats.getSections().clear();
                    exportStats.getSections().addAll(appStats.getSections());
                    exportStats.setSectionPresets(new ArrayList<>(appStats.getSectionPresets()));
                } else if (!selectedSectionIds.isEmpty()) {
                    List<SectionConfig> requiredSections = new ArrayList<>();
                    for (SectionConfig sc : appStats.getSections()) {
                        if (selectedSectionIds.contains(sc.getId())) {
                            requiredSections.add(sc);
                        }
                    }
                    exportStats.getSections().clear();
                    exportStats.getSections().addAll(requiredSections);
                }

                if (exportFlags.getOrDefault("generalConfig", false)) {
                    exportStats.setMatchTitleColor(appStats.isMatchTitleColor());
                    exportStats.setMatchDailyRectColor(appStats.isMatchDailyRectColor());
                    exportStats.setTaskFontSize(appStats.getTaskFontSize());
                    exportStats.setMatchPriorityOutline(appStats.isMatchPriorityOutline());
                    exportStats.setCheckboxTheme(appStats.getCheckboxTheme());
                    exportStats.setMinDailyCompletionPercent(appStats.getMinDailyCompletionPercent());
                    exportStats.setZenModeThreshold(appStats.getZenModeThreshold());
                }

                if (exportFlags.getOrDefault("prioritiesConfig", false)) {
                    exportStats.getCustomPriorities().clear();
                    exportStats.getCustomPriorities().addAll(appStats.getCustomPriorities());
                }

                // 3. Task Database Filtering
                List<TaskItem> exportTasks = new ArrayList<>();
                for (TaskItem task : globalDatabase) {
                    boolean shouldExport = false;

                    if (task.isArchived() && exportFlags.getOrDefault("archived", false)) {
                        shouldExport = true;
                    } else if (!task.isArchived() && selectedSectionIds.contains(task.getSectionId())) {
                        shouldExport = true;
                    }

                    if (shouldExport) {
                        exportTasks.add(task);
                    }
                }

                BackupBundle bundle = new BackupBundle(exportStats, exportTasks);

                // FIXED: Write via Gson to standard text file
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    gson.toJson(bundle, writer);
                }

                Alert success = new Alert(Alert.AlertType.INFORMATION, "Custom backup exported successfully to:\n" + file.getAbsolutePath());
                success.setHeaderText("Export Successful");
                TaskDialogs.styleDialog(success);
                success.show();

            } catch (Exception ex) {
                ex.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR, "Failed to export custom data:\n" + ex.getMessage());
                error.setHeaderText("Export Error");
                TaskDialogs.styleDialog(error);
                error.show();
            }
        }
    }

    /**
     * Imports data from a backup file (Supports both new .json and old .bak files)
     */
    public static void importData(AppStats currentStats, List<TaskItem> currentDatabase, Runnable refreshCallback) {
        Alert warning = new Alert(Alert.AlertType.CONFIRMATION, "WARNING: Importing a backup will OVERWRITE your current data. Are you sure you want to proceed?", ButtonType.YES, ButtonType.NO);
        warning.setHeaderText("Confirm Import");
        TaskDialogs.styleDialog(warning);

        warning.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Import Backup");
                // Allow importing BOTH new JSON and legacy BAK files
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Supported Backups (*.json, *.bak, *.dat)", "*.json", "*.bak", "*.dat"),
                        new FileChooser.ExtensionFilter("JSON Backup (*.json)", "*.json"),
                        new FileChooser.ExtensionFilter("Legacy Backup (*.bak, *.dat)", "*.bak", "*.dat")
                );
                File file = fileChooser.showOpenDialog(null);

                if (file != null) {
                    BackupBundle bundle = null;

                    // Strategy 1: Try reading as modern JSON
                    try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                        bundle = gson.fromJson(reader, BackupBundle.class);
                    } catch (Exception jsonEx) {

                        // Strategy 2: If JSON fails, fallback to Legacy Java Serialization
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                            bundle = (BackupBundle) ois.readObject();
                            System.out.println("Imported using Legacy Binary format.");
                        } catch (Exception binEx) {
                            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to import data. The file format is unrecognized or corrupted.");
                            error.setHeaderText("Import Error");
                            TaskDialogs.styleDialog(error);
                            error.show();
                            return;
                        }
                    }

                    if (bundle != null && bundle.getAppStats() != null && bundle.getTaskDatabase() != null) {
                        currentDatabase.clear();
                        currentDatabase.addAll(bundle.getTaskDatabase());

                        // Overwrite internal storage (This will auto-convert any legacy imports to the new JSON system locally!)
                        StorageManager.saveStats(bundle.getAppStats());
                        StorageManager.saveTasks(currentDatabase);

                        Alert success = new Alert(Alert.AlertType.INFORMATION, "Data imported successfully! The application will now refresh.");
                        success.setHeaderText("Import Successful");
                        TaskDialogs.styleDialog(success);
                        success.showAndWait();

                        refreshCallback.run();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR, "Failed to import data. The backup file is missing required data.");
                        error.setHeaderText("Import Error");
                        TaskDialogs.styleDialog(error);
                        error.show();
                    }
                }
            }
        });
    }
}