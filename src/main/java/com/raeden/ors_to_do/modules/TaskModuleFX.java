package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskModuleFX extends BorderPane {
    private VBox listContainer;
    private TaskItem.OriginModule moduleType;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private TextField inputField;
    private ComboBox<TaskItem.CustomPriority> priorityBox;
    private Label availableTasksLabel;

    private final String[] DARK_PASTELS = {
            "#2C3E50", "#34495E", "#1A252C", "#2D3748", "#2A4365",
            "#2C5282", "#2B6CB0", "#234E52", "#285E61", "#2C7A7B",
            "#22543D", "#276749", "#2F855A", "#744210", "#975A16",
            "#702459", "#97266D", "#44337A", "#553C9A", "#1A202C"
    };

    public TaskModuleFX(TaskItem.OriginModule moduleType, List<TaskItem> globalDatabase, AppStats appStats) {
        this.moduleType = moduleType;
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        setPadding(new Insets(15));

        // --- TOP: Tracking Header ---
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));

        availableTasksLabel = new Label();

        if (moduleType == TaskItem.OriginModule.QUICK) {
            availableTasksLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4EC9B0;"); // Green
            headerBox.getChildren().add(availableTasksLabel);
            setTop(headerBox);
        } else if (moduleType == TaskItem.OriginModule.WORK) {
            availableTasksLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #569CD6;"); // Blue
            headerBox.getChildren().add(availableTasksLabel);
            setTop(headerBox);
        }

        // --- CENTER: Dynamic List ---
        listContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);

        // --- BOTTOM: Input Area ---
        HBox inputPanel = new HBox(10);
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.setPadding(new Insets(15, 0, 0, 0));

        inputField = new TextField();
        inputField.setPromptText("Enter new task...");
        inputField.getStyleClass().add("input-field");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(appStats.getCustomPriorities());
        if (!appStats.getCustomPriorities().isEmpty()) priorityBox.setValue(appStats.getCustomPriorities().get(1)); // Default to second tier
        setupPriorityBoxColors(priorityBox);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-btn");
        Button clearBtn = new Button("Clear");

        inputPanel.getChildren().addAll(inputField, priorityBox, addBtn, clearBtn);

        addBtn.setOnAction(e -> addTask());
        inputField.setOnAction(e -> addTask());
        clearBtn.setOnAction(e -> inputField.clear());

        setCenter(scrollPane);
        setBottom(inputPanel);
        refreshList();
    }

    private void setupPriorityBoxColors(ComboBox<TaskItem.CustomPriority> box) {
        box.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TaskItem.CustomPriority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item.getName()); setStyle("-fx-text-fill: " + item.getColorHex() + "; -fx-font-weight: bold;"); }
            }
        });
        box.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TaskItem.CustomPriority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item.getName()); setStyle("-fx-text-fill: " + item.getColorHex() + "; -fx-font-weight: bold;"); }
            }
        });
    }

    public void refreshList() {
        listContainer.getChildren().clear();
        int availableCount = 0;

        for (TaskItem task : globalDatabase) {
            if (task.getOriginModule() == moduleType && !task.isArchived()) {
                listContainer.getChildren().add(createTaskRow(task));
                if (!task.isFinished()) availableCount++;
            }
        }

        if (moduleType == TaskItem.OriginModule.QUICK || moduleType == TaskItem.OriginModule.WORK) {
            availableTasksLabel.setText("Current available tasks: " + availableCount);
        }
    }

    private HBox createTaskRow(TaskItem task) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.getStyleClass().add("task-row");

        if (task.getColorHex() != null) row.setStyle("-fx-background-color: " + task.getColorHex() + ";");

        // --- Left: Priority Rectangle & Metadata ---
        Rectangle prioRect = new Rectangle(5, 25);
        prioRect.setArcWidth(3); prioRect.setArcHeight(3);
        if (task.getPriority() != null && task.getPriority().getColorHex() != null) {
            prioRect.setFill(Color.web(task.getPriority().getColorHex()));
        } else {
            prioRect.setFill(Color.GRAY);
        }

        Label starLabel = new Label("[⭐]");
        starLabel.getStyleClass().add("task-star");
        starLabel.setVisible(task.isFavorite());
        starLabel.setManaged(task.isFavorite());

        Label dateLabel = new Label("[" + task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "]");
        dateLabel.getStyleClass().add("task-metadata");

        HBox metaBox = new HBox(7, prioRect, starLabel, dateLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        if (moduleType == TaskItem.OriginModule.WORK) {
            Label workTypeLabel = new Label("[" + (task.getWorkType().isEmpty() ? "General" : task.getWorkType()) + "]");
            workTypeLabel.getStyleClass().add("task-metadata");
            metaBox.getChildren().add(workTypeLabel);
        }

        // --- Center: Strikethrough Text ---
        Label textLabel = new Label(task.getTextContent());
        textLabel.setWrapText(true);
        // Explicitly applying standard color with conditional strikethrough, ignoring CSS darkening
        if (task.isFinished()) {
            textLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: #E0E0E0;");
        } else {
            textLabel.setStyle("-fx-strikethrough: false; -fx-text-fill: #E0E0E0;");
        }

        HBox textContainer = new HBox(textLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        // --- Right: Controls ---
        ComboBox<TaskItem.CustomPriority> prioBox = new ComboBox<>();
        prioBox.getItems().addAll(appStats.getCustomPriorities());
        prioBox.setValue(task.getPriority());
        setupPriorityBoxColors(prioBox);
        prioBox.setOnAction(e -> {
            task.setPriority(prioBox.getValue());
            if (prioBox.getValue() != null && prioBox.getValue().getColorHex() != null) {
                prioRect.setFill(Color.web(prioBox.getValue().getColorHex()));
            }
            StorageManager.saveTasks(globalDatabase);
        });

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isFinished());
        checkBox.setOnAction(e -> {
            task.setFinished(checkBox.isSelected());
            StorageManager.saveTasks(globalDatabase);
            refreshList();
        });

        row.getChildren().addAll(metaBox, textContainer, prioBox, checkBox);
        attachContextMenu(row, task);
        return row;
    }

    private void attachContextMenu(HBox row, TaskItem task) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem toggleFav = new MenuItem(task.isFavorite() ? "Remove Favorite" : "Add Favorite");
        toggleFav.setOnAction(e -> { task.setFavorite(!task.isFavorite()); StorageManager.saveTasks(globalDatabase); refreshList(); });

        MenuItem editItem = new MenuItem(appStats.getEditMenuText());
        editItem.setOnAction(e -> showEditDialog(task));

        Menu colorMenu = new Menu("Set Background Color");
        for (String hex : DARK_PASTELS) {
            MenuItem colorItem = new MenuItem("");
            Rectangle colorIcon = new Rectangle(14, 14, Color.web(hex));
            colorIcon.setStroke(Color.BLACK);
            colorItem.setGraphic(colorIcon);
            colorItem.setOnAction(e -> { task.setColorHex(hex); StorageManager.saveTasks(globalDatabase); refreshList(); });
            colorMenu.getItems().add(colorItem);
        }
        MenuItem resetColor = new MenuItem("Reset Background");
        resetColor.setOnAction(e -> { task.setColorHex(null); StorageManager.saveTasks(globalDatabase); refreshList(); });
        colorMenu.getItems().addAll(new SeparatorMenuItem(), resetColor);

        MenuItem archiveItem = new MenuItem(appStats.getArchiveMenuText());
        archiveItem.setOnAction(e -> {
            if(!task.isFinished()) task.setFinished(true);
            task.setArchived(true);
            StorageManager.saveTasks(globalDatabase); refreshList();
        });

        MenuItem deleteItem = new MenuItem(appStats.getDeleteMenuText());
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> { globalDatabase.remove(task); StorageManager.saveTasks(globalDatabase); refreshList(); });

        contextMenu.getItems().addAll(toggleFav, editItem, colorMenu, new SeparatorMenuItem(), archiveItem, deleteItem);
        row.setOnContextMenuRequested(e -> contextMenu.show(row, e.getScreenX(), e.getScreenY()));
    }

    private void showEditDialog(TaskItem task) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField contentField = new TextField(task.getTextContent());
        ComboBox<TaskItem.CustomPriority> prioBox = new ComboBox<>();
        prioBox.getItems().addAll(appStats.getCustomPriorities());
        prioBox.setValue(task.getPriority());
        setupPriorityBoxColors(prioBox);

        grid.add(new Label("Content:"), 0, 0); grid.add(contentField, 1, 0);
        grid.add(new Label("Priority:"), 0, 1); grid.add(prioBox, 1, 1);

        TextField workTypeField = new TextField(task.getWorkType());
        if (moduleType == TaskItem.OriginModule.WORK) {
            grid.add(new Label("Work Type:"), 0, 2); grid.add(workTypeField, 1, 2);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                task.setTextContent(contentField.getText().trim());
                task.setPriority(prioBox.getValue());
                if (moduleType == TaskItem.OriginModule.WORK) task.setWorkType(workTypeField.getText().trim());
                StorageManager.saveTasks(globalDatabase);
                refreshList();
            }
        });
    }

    private void addTask() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        TaskItem newTask = new TaskItem(text, priorityBox.getValue(), moduleType);
        if (moduleType == TaskItem.OriginModule.WORK) newTask.setWorkType("Studio Dev");
        globalDatabase.add(newTask);
        refreshList();
        inputField.clear();
        StorageManager.saveTasks(globalDatabase);
    }
}