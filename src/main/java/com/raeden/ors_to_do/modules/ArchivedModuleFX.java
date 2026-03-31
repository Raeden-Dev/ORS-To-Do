package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

        // --- Header ---
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));

        Label headerLabel = new Label("Archived Tasks");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #569CD6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button clearArchiveBtn = new Button("Empty Archive");
        clearArchiveBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        clearArchiveBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Permanently delete ALL archived tasks?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    globalDatabase.removeIf(TaskItem::isArchived);
                    StorageManager.saveTasks(globalDatabase);
                    refreshList();
                    onStateChangedCallback.run();
                }
            });
        });

        headerBox.getChildren().addAll(headerLabel, spacer, clearArchiveBtn);
        setTop(headerBox);

        // --- List Container ---
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
        boolean hasArchived = false;

        // Sort by completion/creation date so newest archived items are at the top
        List<TaskItem> sortedArchive = globalDatabase.stream()
                .filter(TaskItem::isArchived)
                .sorted((t1, t2) -> {
                    if (t1.getDateCompleted() != null && t2.getDateCompleted() != null) {
                        return t2.getDateCompleted().compareTo(t1.getDateCompleted());
                    }
                    return t2.getDateCreated().compareTo(t1.getDateCreated());
                }).toList();

        for (TaskItem task : sortedArchive) {
            listContainer.getChildren().add(createTaskRow(task));
            hasArchived = true;
        }

        if (!hasArchived) {
            Label emptyLabel = new Label("Your archive is empty.");
            emptyLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            listContainer.getChildren().add(emptyLabel);
        }
    }

    private VBox createTaskRow(TaskItem task) {
        VBox completeRow = new VBox();
        completeRow.getStyleClass().add("task-row");
        completeRow.setStyle("-fx-opacity: 0.7;"); // Make archived items look slightly faded

        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10));

        // --- NEW: Dynamic Section Name Resolution ---
        String sectionName = "Unknown List";
        if (task.getSectionId() != null) {
            Optional<AppStats.SectionConfig> config = appStats.getSections().stream()
                    .filter(c -> c.getId().equals(task.getSectionId()))
                    .findFirst();
            if (config.isPresent()) {
                sectionName = config.get().getName();
            }
        } else if (task.getLegacyOriginModule() != null) {
            sectionName = task.getLegacyOriginModule().name(); // Safety fallback for older tasks
        }

        Label moduleLabel = new Label("[" + sectionName + "]");
        moduleLabel.setStyle("-fx-text-fill: #C586C0; -fx-font-size: " + appStats.getTaskFontSize() + "px; -fx-font-weight: bold;");

        Label dateLabel = new Label();
        if (task.getDateCompleted() != null) {
            dateLabel.setText(" [Done: " + task.getDateCompleted().format(DateTimeFormatter.ofPattern("MMM dd")) + "]");
        } else {
            dateLabel.setText(" [Added: " + task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd")) + "]");
        }
        dateLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: " + (appStats.getTaskFontSize() - 2) + "px;");

        HBox metaBox = new HBox(5, moduleLabel, dateLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        Label textLabel = new Label(task.getTextContent());
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: " + appStats.getTaskFontSize() + "px; -fx-strikethrough: " + task.isFinished() + "; -fx-text-fill: #AAAAAA;");

        HBox textContainer = new HBox(textLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        mainRow.getChildren().addAll(metaBox, textContainer);

        // Render Subtasks if they exist
        if (!task.getSubTasks().isEmpty()) {
            VBox subTaskBox = new VBox(5);
            subTaskBox.setPadding(new Insets(0, 10, 10, 40));

            for (TaskItem.SubTask sub : task.getSubTasks()) {
                Label subText = new Label("- " + sub.getTextContent());
                subText.setWrapText(true);
                int subSize = Math.max(10, appStats.getTaskFontSize() - 2);
                String strike = sub.isFinished() ? "-fx-strikethrough: true; " : "";
                subText.setStyle("-fx-font-size: " + subSize + "px; " + strike + "-fx-text-fill: #858585;");
                subTaskBox.getChildren().add(subText);
            }
            completeRow.getChildren().addAll(mainRow, subTaskBox);
        } else {
            completeRow.getChildren().add(mainRow);
        }

        attachContextMenu(completeRow, task);
        return completeRow;
    }

    private void attachContextMenu(VBox row, TaskItem task) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem unarchiveItem = new MenuItem("Unarchive Task");
        unarchiveItem.setOnAction(e -> {
            task.setArchived(false);
            StorageManager.saveTasks(globalDatabase);
            refreshList();
            onStateChangedCallback.run(); // Refreshes the sidebar/dynamic modules
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