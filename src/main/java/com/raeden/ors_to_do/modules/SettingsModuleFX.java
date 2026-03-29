package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class SettingsModuleFX extends VBox {
    public SettingsModuleFX(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        setSpacing(20);
        setPadding(new Insets(20));

        Label header = new Label("Settings");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // --- Context Menu Settings ---
        VBox menuSettings = new VBox(10);
        menuSettings.getChildren().add(new Label("Customize Context Menu Text:"));

        TextField editText = new TextField(appStats.getEditMenuText());
        TextField archiveText = new TextField(appStats.getArchiveMenuText());
        TextField deleteText = new TextField(appStats.getDeleteMenuText());

        Button saveTextBtn = new Button("Save Menu Texts");
        saveTextBtn.setOnAction(e -> {
            appStats.setEditMenuText(editText.getText());
            appStats.setArchiveMenuText(archiveText.getText());
            appStats.setDeleteMenuText(deleteText.getText());
            StorageManager.saveStats(appStats);
            refreshCallback.run();
        });
        menuSettings.getChildren().addAll(editText, archiveText, deleteText, saveTextBtn);

        // --- Custom Priorities ---
        VBox prioSettings = new VBox(10);
        prioSettings.getChildren().add(new Label("Add Custom Priority:"));

        HBox prioInput = new HBox(10);
        TextField prioName = new TextField(); prioName.setPromptText("Priority Name (e.g. CRITICAL)");
        ColorPicker colorPicker = new ColorPicker();

        Button addPrioBtn = new Button("Add");
        addPrioBtn.setOnAction(e -> {
            if(!prioName.getText().isEmpty()) {
                String hex = String.format("#%02X%02X%02X",
                        (int)(colorPicker.getValue().getRed()*255),
                        (int)(colorPicker.getValue().getGreen()*255),
                        (int)(colorPicker.getValue().getBlue()*255));
                appStats.getCustomPriorities().add(new TaskItem.CustomPriority(prioName.getText(), hex));
                StorageManager.saveStats(appStats);
                refreshCallback.run();
                prioName.clear();
            }
        });
        prioInput.getChildren().addAll(prioName, colorPicker, addPrioBtn);
        prioSettings.getChildren().addAll(prioInput);

        // --- Danger Zone ---
        VBox dangerZone = new VBox(10);
        dangerZone.setStyle("-fx-border-color: #FF6666; -fx-border-width: 1; -fx-padding: 10;");
        Label dangerLabel = new Label("Danger Zone");
        dangerLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-weight: bold;");

        Button wipeQuickBtn = new Button("Wipe Quick To-Do");
        wipeQuickBtn.setOnAction(e -> wipeList(globalDatabase, TaskItem.OriginModule.QUICK, refreshCallback));

        Button wipeAllBtn = new Button("Wipe ALL Tasks");
        wipeAllBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;");
        wipeAllBtn.setOnAction(e -> {
            globalDatabase.clear();
            StorageManager.saveTasks(globalDatabase);
            refreshCallback.run();
        });

        dangerZone.getChildren().addAll(dangerLabel, wipeQuickBtn, wipeAllBtn);
        getChildren().addAll(header, menuSettings, prioSettings, dangerZone);
    }

    private void wipeList(List<TaskItem> db, TaskItem.OriginModule module, Runnable refresh) {
        db.removeIf(task -> task.getOriginModule() == module);
        StorageManager.saveTasks(db);
        refresh.run();
    }
}