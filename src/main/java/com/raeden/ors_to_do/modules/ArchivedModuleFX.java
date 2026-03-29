package com.raeden.ors_to_do.modules;

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
    private Runnable onStateChangedCallback;

    public ArchivedModuleFX(List<TaskItem> globalDatabase, Runnable onStateChangedCallback) {
        this.globalDatabase = globalDatabase;
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

    private HBox createArchiveRow(TaskItem task) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.getStyleClass().add("task-row");

        if (task.getColorHex() != null) {
            row.setStyle("-fx-background-color: " + task.getColorHex() + ";");
        }


        // Metadata
        String dateStr = task.getDateCompleted() != null
                ? task.getDateCompleted().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                : "No Date";

        Label dateLabel = new Label("[" + dateStr + "]");
        dateLabel.getStyleClass().add("task-metadata");

        // Aging Background Logic
        if (task.getDateCompleted() != null) {
            long daysOld = java.time.temporal.ChronoUnit.DAYS.between(task.getDateCompleted(), java.time.LocalDateTime.now());
            if (daysOld < 3) row.setStyle("-fx-background-color: #1a4d2e;"); // Dark Green
            else if (daysOld < 7) row.setStyle("-fx-background-color: #cc5500;"); // Orange
            else row.getStyleClass().add("task-row"); // Default
        }

        // Origin Prefix Color Logic
        Label originLabel = new Label("[" + task.getOriginModule().name() + "]");
        if (task.getOriginModule() == TaskItem.OriginModule.WORK) {
            originLabel.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold;"); // Blue
        } else if (task.getOriginModule() == TaskItem.OriginModule.QUICK) {
            originLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;"); // Green
        }

        // Text
        Label textLabel = new Label(task.getTextContent());
        textLabel.getStyleClass().add("task-text");
        textLabel.setWrapText(true);

        row.getChildren().addAll(dateLabel, originLabel, textLabel);
        attachContextMenu(row, task);

        return row;
    }

    private void attachContextMenu(HBox row, TaskItem task) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem unarchiveItem = new MenuItem("Unarchive Task");
        unarchiveItem.setOnAction(e -> {
            task.setArchived(false);
            StorageManager.saveTasks(globalDatabase);
            refreshList();
            onStateChangedCallback.run(); // Updates the other tabs
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