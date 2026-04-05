package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.SubTask;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ArchivedModule extends BorderPane {
    private VBox listContainer;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private Runnable onStateChangedCallback;

    public ArchivedModule(List<TaskItem> globalDatabase, AppStats appStats, Runnable onStateChangedCallback) {
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
            // --- NEW: 3-way Export & Delete Prompt ---
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to export your archived tasks to a text file before deleting them permanently?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.setHeaderText("Export Archive Data?");

            // Inject dark theme
            com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs.styleDialog(alert);

            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.CANCEL) return;

                if (res == ButtonType.YES) {
                    javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                    fileChooser.setTitle("Export Archive Data");
                    fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));
                    fileChooser.setInitialFileName("Archived_Tasks.txt");
                    java.io.File file = fileChooser.showSaveDialog(null);

                    if (file != null) {
                        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                            writer.println("--- ARCHIVED TASKS EXPORT ---");
                            for (TaskItem t : globalDatabase) {
                                if (t.isArchived()) {
                                    String dateStr = t.getDateCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    writer.println("[" + dateStr + "] " + t.getTextContent());
                                }
                            }
                            com.raeden.ors_to_do.modules.dependencies.services.SystemTrayManager.pushNotification("Export Successful", "Archive exported to " + file.getName());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        // Abort deletion if the user cancelled the file save dialog
                        return;
                    }
                }

                // Proceed with deletion
                int toDelete = (int) globalDatabase.stream().filter(TaskItem::isArchived).count();
                globalDatabase.removeIf(TaskItem::isArchived);
                appStats.setLifetimeDeletedTasks(appStats.getLifetimeDeletedTasks() + toDelete);

                StorageManager.saveTasks(globalDatabase);
                StorageManager.saveStats(appStats);
                refreshList();
                onStateChangedCallback.run();
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
        completeRow.setStyle("-fx-opacity: 0.7;");

        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10));

        String sectionName = "Unknown List";
        String sectionColor = "#C586C0";

        if (task.getSectionId() != null) {
            Optional<SectionConfig> config = appStats.getSections().stream()
                    .filter(c -> c.getId().equals(task.getSectionId()))
                    .findFirst();
            if (config.isPresent()) {
                sectionName = config.get().getName();
                sectionColor = config.get().getSidebarColor();
            }
        } else if (task.getLegacyOriginModule() != null) {
            sectionName = task.getLegacyOriginModule().name();
        }

        Label moduleLabel = new Label("[" + sectionName + "]");
        moduleLabel.setStyle("-fx-text-fill: " + sectionColor + "; -fx-font-size: " + appStats.getTaskFontSize() + "px; -fx-font-weight: bold;");

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

        if (task.getRewardPoints() > 0 || task.getPenaltyPoints() > 0) {
            String badgeStr = "";
            if (task.getRewardPoints() > 0) badgeStr += "🏆 +" + task.getRewardPoints() + "  ";
            if (task.getPenaltyPoints() > 0) badgeStr += "💀 -" + task.getPenaltyPoints();

            Label ptsLabel = new Label(badgeStr.trim());
            ptsLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 12px;");
            ptsLabel.setPadding(new Insets(0, 10, 0, 0));
            mainRow.getChildren().add(ptsLabel);
        }

        if (task.getTimeSpentSeconds() > 0) {
            int mins = task.getTimeSpentSeconds() / 60;
            Label timeLabel = new Label("⏱ " + mins + "m");
            timeLabel.setPadding(new Insets(0, 10, 0, 0));
            timeLabel.setStyle("-fx-text-fill: #E06666; -fx-font-weight: bold; -fx-font-size: 13px;");
            mainRow.getChildren().add(timeLabel);
        }

        if (!task.getSubTasks().isEmpty()) {
            VBox subTaskBox = new VBox(5);
            subTaskBox.setPadding(new Insets(0, 10, 10, 40));

            for (SubTask sub : task.getSubTasks()) {
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
            onStateChangedCallback.run();
        });

        MenuItem deleteItem = new MenuItem("Permanently Delete");
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> {
            globalDatabase.remove(task);
            appStats.setLifetimeDeletedTasks(appStats.getLifetimeDeletedTasks() + 1);
            StorageManager.saveTasks(globalDatabase);
            StorageManager.saveStats(appStats);
            refreshList();
        });

        contextMenu.getItems().addAll(unarchiveItem, new SeparatorMenuItem(), deleteItem);
        row.setOnContextMenuRequested(e -> contextMenu.show(row, e.getScreenX(), e.getScreenY()));
    }
}