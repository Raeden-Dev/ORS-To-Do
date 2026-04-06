package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionRow extends HBox {

    public SectionRow(SectionConfig config, int index, AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshList, Runnable onSectionChanged, Runnable refreshCallback) {
        super(10);
        setAlignment(Pos.CENTER_LEFT);

        Rectangle colorIndicator = new Rectangle(12, 12, Color.web(config.getSidebarColor()));
        colorIndicator.setStroke(Color.web("#555555"));

        Label nameLabel = new Label(config.getName());
        nameLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox nameBox = new HBox(10, colorIndicator, nameLabel);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        if (config.getResetIntervalHours() > 0) {
            Label intervalBadge = new Label("⏱ " + config.getResetIntervalHours() + "h Reset");
            intervalBadge.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px; -fx-background-color: #2D2D30; -fx-padding: 3 8; -fx-background-radius: 10; -fx-border-color: #3E3E42; -fx-border-radius: 10;");
            nameBox.getChildren().add(intervalBadge);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("⚙ Edit Features");
        editBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
        editBtn.setOnAction(e -> SectionEditDialog.show(config, false, appStats, () -> {
            StorageManager.saveStats(appStats);
            refreshList.run();
            if(onSectionChanged != null) onSectionChanged.run();
            refreshCallback.run();
        }));

        Button moveUpBtn = new Button("▲");
        moveUpBtn.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-border-color: #3E3E42; -fx-border-radius: 3;");
        moveUpBtn.setOnAction(e -> moveSection(config, index, -1, appStats, refreshList, onSectionChanged, refreshCallback));

        Button moveDownBtn = new Button("▼");
        moveDownBtn.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-border-color: #3E3E42; -fx-border-radius: 3;");
        moveDownBtn.setOnAction(e -> moveSection(config, index, 1, appStats, refreshList, onSectionChanged, refreshCallback));

        Button deleteBtn = new Button("❌");
        deleteBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteSection(config, appStats, globalDatabase, refreshList, onSectionChanged, refreshCallback));

        getChildren().addAll(nameBox, spacer, moveUpBtn, moveDownBtn, editBtn, deleteBtn);
    }

    private void moveSection(SectionConfig config, int currentIndex, int offset, AppStats appStats, Runnable refreshList, Runnable onSectionChanged, Runnable refreshCallback) {
        int newIndex = currentIndex + offset;
        if (newIndex >= 0 && newIndex < appStats.getSections().size()) {
            Collections.swap(appStats.getSections(), currentIndex, newIndex);
            StorageManager.saveStats(appStats);
            refreshList.run();
            if(onSectionChanged != null) onSectionChanged.run();
            refreshCallback.run();
        }
    }

    private void deleteSection(SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshList, Runnable onSectionChanged, Runnable refreshCallback) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete section '" + config.getName() + "'?\n\nThis will permanently delete all tasks inside it!", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Delete Section");
        TaskDialogs.styleDialog(alert);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                appStats.getSections().remove(config);

                List<TaskItem> toRemove = new ArrayList<>();
                for (TaskItem task : globalDatabase) {
                    if (config.getId().equals(task.getSectionId())) {
                        toRemove.add(task);
                    }
                }
                globalDatabase.removeAll(toRemove);

                StorageManager.saveStats(appStats);
                StorageManager.saveTasks(globalDatabase);
                refreshList.run();
                if(onSectionChanged != null) onSectionChanged.run();
                refreshCallback.run();
            }
        });
    }
}