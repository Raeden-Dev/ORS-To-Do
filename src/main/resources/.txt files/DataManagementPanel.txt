package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.services.BackupManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;

public class DataManagementPanel extends VBox {

    public DataManagementPanel(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        super(15);
        setStyle("-fx-border-color: #2E8B57; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label header = new Label("Data Management (Backup & Restore)");
        header.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 16px; -fx-font-weight: bold;");

        // --- FIXED: HBox layout to stretch buttons evenly ---
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button exportBtn = new Button("Export All Data");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(exportBtn, Priority.ALWAYS); // Stretches button across available space
        exportBtn.setStyle("-fx-background-color: #1a4d33; -fx-text-fill: #4EC9B0; -fx-border-color: #4EC9B0; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; -fx-padding: 8 15;");

        Button importBtn = new Button("Import Backup Data");
        importBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(importBtn, Priority.ALWAYS); // Stretches button across available space
        importBtn.setStyle("-fx-background-color: #1a4d33; -fx-text-fill: #4EC9B0; -fx-border-color: #4EC9B0; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; -fx-padding: 8 15;");

        // --- FIXED: Using the CORRECT method names from your BackupManager ---
        exportBtn.setOnAction(e -> BackupManager.exportData(appStats, globalDatabase));
        importBtn.setOnAction(e -> BackupManager.importData(appStats, globalDatabase, refreshCallback));

        buttonBox.getChildren().addAll(exportBtn, importBtn);
        getChildren().addAll(header, buttonBox);
    }
}