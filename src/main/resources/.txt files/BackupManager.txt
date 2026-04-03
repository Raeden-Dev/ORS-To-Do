package com.raeden.ors_to_do.modules.dependencies.services;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.List;
import java.util.Optional;

public class BackupManager {

    public static void exportData(AppStats appStats, List<TaskItem> taskDatabase) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export ORS Data Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ORS Backup File", "*.orsb"));
        fileChooser.setInitialFileName("ORS_Backup.orsb");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(new BackupBundle(appStats, taskDatabase));
                SystemTrayManager.pushNotification("Export Successful", "All data safely exported to: " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Failed", "Could not write to the selected file.");
            }
        }
    }

    public static void importData(AppStats currentStats, List<TaskItem> currentTasks, Runnable onComplete) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import ORS Data Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ORS Backup File", "*.orsb"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                BackupBundle bundle = (BackupBundle) ois.readObject();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Import Data");
                alert.setHeaderText("How would you like to import this backup?");
                alert.setContentText("- REPLACE: Wipes current data and loads the backup entirely.\n- MERGE: Adds missing tasks/sections and keeps your highest stats.");

                ButtonType replaceBtn = new ButtonType("Replace All");
                ButtonType mergeBtn = new ButtonType("Merge Data");
                ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(replaceBtn, mergeBtn, cancelBtn);
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() != cancelBtn) {
                    if (result.get() == replaceBtn) {
                        currentStats.copyFrom(bundle.getAppStats());
                        currentTasks.clear();
                        currentTasks.addAll(bundle.getTaskDatabase());
                    } else if (result.get() == mergeBtn) {
                        mergeData(currentStats, currentTasks, bundle.getAppStats(), bundle.getTaskDatabase());
                    }

                    StorageManager.saveStats(currentStats);
                    StorageManager.saveTasks(currentTasks);
                    onComplete.run();
                    SystemTrayManager.pushNotification("Import Successful", "Your data has been loaded.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showError("Import Failed", "The selected file is invalid or corrupted.");
            }
        }
    }

    private static void mergeData(AppStats currentStats, List<TaskItem> currentTasks, AppStats importedStats, List<TaskItem> importedTasks) {
        // Merge missing tasks
        for (TaskItem importedTask : importedTasks) {
            boolean exists = currentTasks.stream().anyMatch(t -> t.getId().equals(importedTask.getId()));
            if (!exists) currentTasks.add(importedTask);
        }

        // Merge missing sections
        for (SectionConfig importedSec : importedStats.getSections()) {
            boolean exists = currentStats.getSections().stream().anyMatch(s -> s.getId().equals(importedSec.getId()));
            if (!exists) currentStats.getSections().add(importedSec);
        }

        // Merge missing priorities
        for (CustomPriority importedPrio : importedStats.getCustomPriorities()) {
            boolean exists = currentStats.getCustomPriorities().stream().anyMatch(p -> p.getName().equals(importedPrio.getName()));
            if (!exists) currentStats.getCustomPriorities().add(importedPrio);
        }

        // Take highest stats
        currentStats.setGlobalScore(Math.max(currentStats.getGlobalScore(), importedStats.getGlobalScore()));
        currentStats.setHighestStreak(Math.max(currentStats.getHighestStreak(), importedStats.getHighestStreak()));
        currentStats.setLifetimeDeletedTasks(currentStats.getLifetimeDeletedTasks() + importedStats.getLifetimeDeletedTasks());
    }

    private static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}