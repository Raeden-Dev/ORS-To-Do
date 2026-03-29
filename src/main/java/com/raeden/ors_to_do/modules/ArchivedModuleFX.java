package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ArchivedModuleFX extends BorderPane {
    private VBox listContainer;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private Runnable onStateChangedCallback;

    public ArchivedModuleFX(List<TaskItem> globalDatabase, AppStats appStats, Runnable onStateChangedCallback) {
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        this.onStateChangedCallback = onStateChangedCallback;
        setPadding(new Insets(15));

        listContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);

        setCenter(scrollPane);
        refreshList();
    }

    public void refreshList() {
        listContainer.getChildren().clear();
        for (TaskItem task : globalDatabase) {
            if (task.isArchived()) {
                listContainer.getChildren().add(createArchiveRow(task));
            }
        }
    }

    // --- CHANGED: Now returns a VBox to show historical sub-tasks ---
    private VBox createArchiveRow(TaskItem task) {
        VBox completeRow = new VBox();
        completeRow.getStyleClass().add("task-row");

        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10));

        if (task.getColorHex() != null) {
            completeRow.setStyle("-fx-background-color: " + task.getColorHex() + ";");
        }

        String dateStr = task.getDateCompleted() != null
                ? task.getDateCompleted().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                : "No Date";

        Label dateLabel = new Label("[" + dateStr + "]");
        dateLabel.getStyleClass().add("task-metadata");

        if (task.getDateCompleted() != null) {
            long daysOld = java.time.temporal.ChronoUnit.DAYS.between(task.getDateCompleted(), java.time.LocalDateTime.now());
            if (daysOld < 3) completeRow.setStyle("-fx-background-color: #1a4d2e;");
            else if (daysOld < 7) completeRow.setStyle("-fx-background-color: #cc5500;");
        }

        Label originLabel = new Label("[" + task.getOriginModule().name() + "]");
        if (task.getOriginModule() == TaskItem.OriginModule.WORK) {
            originLabel.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold;");
        } else if (task.getOriginModule() == TaskItem.OriginModule.QUICK) {
            originLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");
        }

        Label textLabel = new Label(task.getTextContent());
        textLabel.getStyleClass().add("task-text");
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: " + appStats.getTaskFontSize() + "px; -fx-text-fill: #E0E0E0;");

        HBox textContainer = new HBox(textLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Label timeLabel = null;
        if (task.getTimeSpentSeconds() > 0) {
            int mins = task.getTimeSpentSeconds() / 60;
            timeLabel = new Label("⏱ " + mins + "m");
            timeLabel.setStyle("-fx-text-fill: #E06666; -fx-font-weight: bold; -fx-font-size: 13px;");
        }

        if (timeLabel != null) {
            mainRow.getChildren().addAll(dateLabel, originLabel, textContainer, timeLabel);
        } else {
            mainRow.getChildren().addAll(dateLabel, originLabel, textContainer);
        }
        attachContextMenu(mainRow, task);

        // --- NEW: Render Historical Sub-Tasks ---
        VBox subTaskBox = new VBox(5);
        subTaskBox.setPadding(new Insets(0, 10, 10, 50));

        if (!task.getSubTasks().isEmpty()) {
            for (TaskItem.SubTask sub : task.getSubTasks()) {
                Label subText = new Label("• " + sub.getTextContent());
                int subSize = Math.max(10, appStats.getTaskFontSize() - 2);
                String subFontStyle = "-fx-font-size: " + subSize + "px; ";

                if (sub.isFinished()) {
                    subText.setStyle(subFontStyle + "-fx-strikethrough: true; -fx-text-fill: #858585;");
                } else {
                    subText.setStyle(subFontStyle + "-fx-strikethrough: false; -fx-text-fill: #CCCCCC;");
                }
                subTaskBox.getChildren().add(subText);
            }
            completeRow.getChildren().addAll(mainRow, subTaskBox);
        } else {
            completeRow.getChildren().add(mainRow);
        }

        return completeRow;
    }

    private void attachContextMenu(HBox row, TaskItem task) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem unarchiveItem = new MenuItem("Unarchive Task");
        unarchiveItem.setOnAction(e -> {
            task.setArchived(false);
            StorageManager.saveTasks(globalDatabase);
            refreshList();
            onStateChangedCallback.run();
        });

        MenuItem deleteItem = new MenuItem("Permanently Delete");
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> {
            globalDatabase.remove(task);
            StorageManager.saveTasks(globalDatabase);
            refreshList();
        });

        contextMenu.getItems().addAll(unarchiveItem, new SeparatorMenuItem(), deleteItem);
        row.setOnContextMenuRequested(e -> contextMenu.show(row, e.getScreenX(), e.getScreenY()));
    }
}