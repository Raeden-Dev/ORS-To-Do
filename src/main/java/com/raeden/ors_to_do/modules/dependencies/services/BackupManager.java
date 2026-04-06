package com.raeden.ors_to_do.modules.dependencies.services;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackupManager {

    /**
     * Exports ALL current application data to a backup file.
     */
    public static void exportData(AppStats appStats, List<TaskItem> globalDatabase) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Full Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Task Tracker Backup", "*.bak", "*.dat"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                BackupBundle bundle = new BackupBundle(appStats, globalDatabase);
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(bundle);
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
     * Exports only specific slices of data based on the user's checkbox selections.
     */
    public static void exportCustomData(AppStats appStats, List<TaskItem> globalDatabase, Map<String, Boolean> exportFlags, List<String> selectedSectionIds) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Custom Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Task Tracker Backup", "*.bak", "*.dat"));
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

                    // Use get().putAll() instead of missing setters
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
                    // Safety Fallback: Export required section configs for exported tasks
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

                // 4. Package and Save
                BackupBundle bundle = new BackupBundle(exportStats, exportTasks);

                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(bundle);
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
     * Imports data from a backup file, replacing current data.
     */
    public static void importData(AppStats currentStats, List<TaskItem> currentDatabase, Runnable refreshCallback) {
        Alert warning = new Alert(Alert.AlertType.CONFIRMATION, "WARNING: Importing a backup will OVERWRITE your current data. Are you sure you want to proceed?", ButtonType.YES, ButtonType.NO);
        warning.setHeaderText("Confirm Import");
        TaskDialogs.styleDialog(warning);

        warning.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Import Backup");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Task Tracker Backup", "*.bak", "*.dat"));
                File file = fileChooser.showOpenDialog(null);

                if (file != null) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        BackupBundle bundle = (BackupBundle) ois.readObject();

                        if (bundle.getAppStats() != null && bundle.getTaskDatabase() != null) {

                            // Clear existing data
                            currentDatabase.clear();
                            currentDatabase.addAll(bundle.getTaskDatabase());

                            StorageManager.saveStats(bundle.getAppStats());
                            StorageManager.saveTasks(currentDatabase);

                            Alert success = new Alert(Alert.AlertType.INFORMATION, "Data imported successfully! The application will now refresh.");
                            success.setHeaderText("Import Successful");
                            TaskDialogs.styleDialog(success);
                            success.showAndWait();

                            // Trigger full UI refresh
                            refreshCallback.run();
                        } else {
                            throw new Exception("Invalid backup file format.");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Alert error = new Alert(Alert.AlertType.ERROR, "Failed to import data. The file may be from an older, incompatible version of the app.\n\nError: " + ex.getMessage());
                        error.setHeaderText("Import Error");
                        TaskDialogs.styleDialog(error);
                        error.show();
                    }
                }
            }
        });
    }
}