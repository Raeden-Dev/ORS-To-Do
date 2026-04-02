package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.utils.BackupManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class DataManagementPanel extends VBox {
    private final double BUTTON_WIDTH = 200.0;

    public DataManagementPanel(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        super(15);
        setStyle("-fx-border-color: #4EC9B0; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label header = new Label("Data Management (Backup & Restore)");
        header.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        Button exportBtn = new Button("Export All Data");
        exportBtn.setPrefWidth(BUTTON_WIDTH);
        exportBtn.setStyle("-fx-background-color: #22543D; -fx-text-fill: #4EC9B0; -fx-border-color: #4EC9B0; -fx-border-radius: 3; -fx-cursor: hand;");
        exportBtn.setOnAction(e -> BackupManager.exportData(appStats, globalDatabase));

        Button importBtn = new Button("Import Backup Data");
        importBtn.setPrefWidth(BUTTON_WIDTH);
        importBtn.setStyle("-fx-background-color: #22543D; -fx-text-fill: #4EC9B0; -fx-border-color: #4EC9B0; -fx-border-radius: 3; -fx-cursor: hand;");
        // Passing the refresh callback heavily ensures everything updates immediately when import finishes
        importBtn.setOnAction(e -> BackupManager.importData(appStats, globalDatabase, refreshCallback));

        grid.add(exportBtn, 0, 0);
        grid.add(importBtn, 1, 0);

        getChildren().addAll(header, grid);
    }
}