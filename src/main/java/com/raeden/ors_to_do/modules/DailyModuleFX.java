package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.util.List;

public class DailyModuleFX extends BorderPane {
    private VBox listContainer;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private TextField inputField, prefixField;

    public DailyModuleFX(List<TaskItem> globalDatabase, AppStats appStats) {
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        setPadding(new Insets(15));

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));

        Label streakLabel = new Label("🔥 " + appStats.getCurrentStreak() + " Day Streak");
        streakLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FF8C00;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button historyBtn = new Button("Export History");
        historyBtn.getStyleClass().add("action-btn");
        historyBtn.setOnAction(e -> exportHistoryToCSV());

        headerBox.getChildren().addAll(streakLabel, spacer, historyBtn);

        listContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);

        HBox inputPanel = new HBox(10);
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.setPadding(new Insets(15, 0, 0, 0));

        prefixField = new TextField();
        prefixField.setPromptText("[GYM]");
        prefixField.setPrefWidth(80);
        prefixField.getStyleClass().add("input-field");

        inputField = new TextField();
        inputField.setPromptText("Enter daily task...");
        inputField.getStyleClass().add("input-field");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-btn");

        inputPanel.getChildren().addAll(prefixField, inputField, addBtn);

        addBtn.setOnAction(e -> addTask());
        inputField.setOnAction(e -> addTask());

        setTop(headerBox);
        setCenter(scrollPane);
        setBottom(inputPanel);
        refreshList();
    }

    public void refreshList() {
        listContainer.getChildren().clear();
        for (TaskItem task : globalDatabase) {
            if (task.getOriginModule() == TaskItem.OriginModule.DAILY && !task.isArchived()) {
                listContainer.getChildren().add(createTaskRow(task));
            }
        }
    }

    private HBox createTaskRow(TaskItem task) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.getStyleClass().add("task-row");

        if (task.getColorHex() != null) row.setStyle("-fx-background-color: " + task.getColorHex() + ";");

        Label starLabel = new Label("[⭐]");
        starLabel.getStyleClass().add("task-star");
        starLabel.setVisible(task.isFavorite());
        starLabel.setManaged(task.isFavorite());

        HBox metaBox = new HBox(5, starLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        if (task.getPrefix() != null && !task.getPrefix().isEmpty()) {
            Label prefixLabel = new Label(task.getPrefix());
            prefixLabel.getStyleClass().add("task-prefix");
            metaBox.getChildren().add(prefixLabel);
        }

        Label textLabel = new Label(task.getTextContent());
        textLabel.getStyleClass().add(task.isFinished() ? "task-text-finished" : "task-text");

        HBox textContainer = new HBox(textLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isFinished());
        checkBox.setOnAction(e -> {
            task.setFinished(checkBox.isSelected());
            StorageManager.saveTasks(globalDatabase);
            refreshList();
        });

        row.getChildren().addAll(metaBox, textContainer, checkBox); // No Priority Box!
        attachContextMenu(row, task);
        return row;
    }

    private void attachContextMenu(HBox row, TaskItem task) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem toggleFav = new MenuItem(task.isFavorite() ? "Remove Favorite" : "Add Favorite");
        toggleFav.setOnAction(e -> { task.setFavorite(!task.isFavorite()); StorageManager.saveTasks(globalDatabase); refreshList(); });

        MenuItem editItem = new MenuItem(appStats.getEditMenuText());
        editItem.setOnAction(e -> showEditDialog(task));

        MenuItem deleteItem = new MenuItem(appStats.getDeleteMenuText());
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> { globalDatabase.remove(task); StorageManager.saveTasks(globalDatabase); refreshList(); });

        contextMenu.getItems().addAll(toggleFav, editItem, new SeparatorMenuItem(), deleteItem);
        row.setOnContextMenuRequested(e -> contextMenu.show(row, e.getScreenX(), e.getScreenY()));
    }

    private void showEditDialog(TaskItem task) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Daily Task");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField contentField = new TextField(task.getTextContent());
        TextField prefixEditField = new TextField(task.getPrefix());

        grid.add(new Label("Prefix:"), 0, 0); grid.add(prefixEditField, 1, 0);
        grid.add(new Label("Content:"), 0, 1); grid.add(contentField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                task.setTextContent(contentField.getText().trim());
                task.setPrefix(prefixEditField.getText().trim());
                StorageManager.saveTasks(globalDatabase);
                refreshList();
            }
        });
    }

    private void exportHistoryToCSV() {
        try {
            File exportFile = new File(System.getProperty("user.home") + "/Desktop/DailyHistory.csv");
            java.io.FileWriter writer = new java.io.FileWriter(exportFile);
            writer.append("Date,Total Tasks,Completed Tasks,Completion %\n");

            for (var entry : appStats.getAdvancedHistoryLog().entrySet()) {
                int[] stats = entry.getValue();
                double percent = stats[0] == 0 ? 0 : ((double)stats[1] / stats[0]) * 100;
                writer.append(entry.getKey() + "," + stats[0] + "," + stats[1] + "," + String.format("%.1f", percent) + "%\n");
            }
            writer.flush();
            writer.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Exported to Desktop: DailyHistory.csv");
            alert.setHeaderText(null);
            alert.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addTask() {
        String text = inputField.getText().trim();
        String prefix = prefixField.getText().trim();
        if (text.isEmpty()) return;

        // Give it the first priority as a hidden default to prevent null errors
        TaskItem.CustomPriority defaultPrio = appStats.getCustomPriorities().isEmpty() ? null : appStats.getCustomPriorities().get(0);
        TaskItem newTask = new TaskItem(text, defaultPrio, TaskItem.OriginModule.DAILY);

        if (!prefix.isEmpty()) {
            String cleanPrefix = prefix.toUpperCase();
            if (!cleanPrefix.startsWith("[")) cleanPrefix = "[" + cleanPrefix;
            if (!cleanPrefix.endsWith("]")) cleanPrefix = cleanPrefix + "]";
            newTask.setPrefix(cleanPrefix);
        }

        globalDatabase.add(newTask);
        refreshList();
        inputField.clear();
        StorageManager.saveTasks(globalDatabase);
    }
}