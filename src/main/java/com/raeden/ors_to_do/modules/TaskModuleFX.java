package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskModuleFX extends BorderPane {
    private VBox listContainer;
    private TaskItem.OriginModule moduleType;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private TextField inputField;
    private ComboBox<TaskItem.CustomPriority> priorityBox;
    private Label availableTasksLabel;

    // Tagging, Filters, & Sorting
    private FlowPane filterContainer;
    private String activeFilter = "All";
    private ComboBox<String> sortComboBox;

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

        // --- TOP: Tracking Header & Filters ---
        VBox topArea = new VBox(10);

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        availableTasksLabel = new Label();

        if (moduleType == TaskItem.OriginModule.QUICK) {
            availableTasksLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4EC9B0;");
        } else if (moduleType == TaskItem.OriginModule.WORK) {
            availableTasksLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #569CD6;");
        }
        headerBox.getChildren().add(availableTasksLabel);

        // Filter UI
        filterContainer = new FlowPane(5, 5);

        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Custom Order", "Most Recent", "Oldest First", "Alphabetical", "Priority: Low to High", "Priority: High to Low");
        sortComboBox.setValue("Custom Order"); // FIXED: Default to Custom to allow dragging immediately
        sortComboBox.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
        sortComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: black;");
            }
        });
        sortComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: white;");
            }
        });
        sortComboBox.setOnAction(e -> refreshList());

        HBox filterSortRow = new HBox(10);
        filterSortRow.setAlignment(Pos.CENTER_LEFT);
        filterSortRow.setPadding(new Insets(0, 0, 10, 0));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Pushes the Sort Box to the far right

        // --- NEW: Add Export Button specifically to Work List ---
        if (moduleType == TaskItem.OriginModule.WORK) {
            Button exportBtn = new Button("📊 Export Analytics");
            exportBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            exportBtn.setOnAction(e -> exportWorkAnalytics());
            filterSortRow.getChildren().addAll(filterContainer, spacer, exportBtn, sortComboBox);
        } else {
            filterSortRow.getChildren().addAll(filterContainer, spacer, sortComboBox);
        }

        topArea.getChildren().addAll(headerBox, filterSortRow);
        setTop(topArea);

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
        if (!appStats.getCustomPriorities().isEmpty()) priorityBox.setValue(appStats.getCustomPriorities().get(1));
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

    private int getPriorityWeight(TaskItem.CustomPriority p) {
        if (p == null) return -1;
        int idx = appStats.getCustomPriorities().indexOf(p);
        return idx == -1 ? 999 : idx;
    }

    public void refreshList() {
        listContainer.getChildren().clear();
        int availableCount = 0;

        Set<String> uniqueTags = new HashSet<>();
        List<TaskItem> tasksToDisplay = new ArrayList<>();

        for (TaskItem task : globalDatabase) {
            if (task.getOriginModule() == moduleType && !task.isArchived()) {
                String tag = (moduleType == TaskItem.OriginModule.WORK) ? task.getWorkType() : task.getPrefix();
                if (tag != null && !tag.trim().isEmpty()) uniqueTags.add(tag);

                boolean passesFilter = activeFilter.equals("All") || (tag != null && tag.equals(activeFilter));

                if (passesFilter) {
                    tasksToDisplay.add(task);
                    if (!task.isFinished()) availableCount++;
                }
            }
        }

        String sortMode = sortComboBox.getValue();
        if (!sortMode.equals("Custom Order")) {
            tasksToDisplay.sort((t1, t2) -> {
                switch (sortMode) {
                    case "Oldest First": return t1.getDateCreated().compareTo(t2.getDateCreated());
                    case "Alphabetical": return t1.getTextContent().compareToIgnoreCase(t2.getTextContent());
                    case "Priority: Low to High": return Integer.compare(getPriorityWeight(t1.getPriority()), getPriorityWeight(t2.getPriority()));
                    case "Priority: High to Low": return Integer.compare(getPriorityWeight(t2.getPriority()), getPriorityWeight(t1.getPriority()));
                    case "Most Recent":
                    default: return t2.getDateCreated().compareTo(t1.getDateCreated());
                }
            });
        }

        // --- FIXED: Empty State UI ---
        if (tasksToDisplay.isEmpty()) {
            Label emptyLabel = new Label("Add a task to get started!");
            emptyLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            listContainer.getChildren().add(emptyLabel);
        } else {
            for (TaskItem task : tasksToDisplay) {
                listContainer.getChildren().add(createTaskRow(task));
            }
        }

        availableTasksLabel.setText("Current available tasks: " + availableCount);
        updateFilterPills(uniqueTags);
    }

    private void updateFilterPills(Set<String> uniqueTags) {
        filterContainer.getChildren().clear();

        ToggleGroup filterGroup = new ToggleGroup();

        ToggleButton allBtn = new ToggleButton("All");
        allBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
        allBtn.setToggleGroup(filterGroup);
        if (activeFilter.equals("All")) allBtn.setSelected(true);
        allBtn.setOnAction(e -> { activeFilter = "All"; refreshList(); });
        filterContainer.getChildren().add(allBtn);

        List<String> sortedTags = new ArrayList<>(uniqueTags);
        Collections.sort(sortedTags);

        for (String tag : sortedTags) {
            ToggleButton tagBtn = new ToggleButton(tag);
            tagBtn.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-border-color: #569CD6; -fx-border-radius: 3; -fx-cursor: hand;");
            tagBtn.setToggleGroup(filterGroup);
            if (activeFilter.equals(tag)) tagBtn.setSelected(true);

            tagBtn.setOnAction(e -> {
                if (tagBtn.isSelected()) activeFilter = tag;
                else activeFilter = "All";
                refreshList();
            });
            filterContainer.getChildren().add(tagBtn);
        }
    }

    private VBox createTaskRow(TaskItem task) {
        VBox completeRow = new VBox();
        completeRow.getStyleClass().add("task-row");
        String originalStyle = task.getColorHex() != null ? "-fx-background-color: " + task.getColorHex() + ";" : "";
        completeRow.setStyle(originalStyle);

        completeRow.setOnDragDetected(event -> {
            Dragboard db = completeRow.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getId());
            db.setContent(content);
            event.consume();
        });

        completeRow.setOnDragOver(event -> {
            if (event.getGestureSource() != completeRow && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        completeRow.setOnDragEntered(event -> {
            if (event.getGestureSource() != completeRow && event.getDragboard().hasString()) {
                completeRow.setStyle(originalStyle + " -fx-border-color: #569CD6; -fx-border-width: 2;");
            }
        });

        completeRow.setOnDragExited(event -> {
            completeRow.setStyle(originalStyle);
        });

        completeRow.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String draggedId = db.getString();
                reorderTasks(draggedId, task.getId());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10));

        Button expandBtn = new Button(task.isExpanded() ? "▼" : "▶");
        expandBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-padding: 0 5 0 0; -fx-cursor: hand;");
        if (task.getSubTasks().isEmpty()) { expandBtn.setVisible(false); expandBtn.setManaged(false); }
        expandBtn.setOnAction(e -> { task.setExpanded(!task.isExpanded()); StorageManager.saveTasks(globalDatabase); refreshList(); });

        Rectangle prioRect = new Rectangle(5, 25);
        prioRect.setArcWidth(3); prioRect.setArcHeight(3);
        if (task.getPriority() != null && task.getPriority().getColorHex() != null) prioRect.setFill(Color.web(task.getPriority().getColorHex()));
        else prioRect.setFill(Color.GRAY);

        Label starLabel = new Label("[⭐]");
        starLabel.getStyleClass().add("task-star");
        starLabel.setVisible(task.isFavorite());
        starLabel.setManaged(task.isFavorite());

        Label dateLabel = new Label("[" + task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "]");
        dateLabel.getStyleClass().add("task-metadata");

        HBox metaBox = new HBox(7, expandBtn, prioRect, starLabel, dateLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        if (moduleType == TaskItem.OriginModule.WORK && !task.getWorkType().isEmpty()) {
            Label workTypeLabel = new Label("[" + task.getWorkType() + "]");
            workTypeLabel.getStyleClass().add("task-metadata");
            metaBox.getChildren().add(workTypeLabel);
        }

        Label textLabel = new Label(task.getTextContent());
        textLabel.setWrapText(true);
        String fontStyle = "-fx-font-size: " + appStats.getTaskFontSize() + "px; ";
        if (task.isFinished()) textLabel.setStyle(fontStyle + "-fx-strikethrough: true; -fx-text-fill: #E0E0E0;");
        else textLabel.setStyle(fontStyle + "-fx-strikethrough: false; -fx-text-fill: #E0E0E0;");

        HBox textContainer = new HBox(textLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        int mins = task.getTimeSpentSeconds() / 60;
        Label timeLabel = new Label("⏱ " + mins + "m");
        timeLabel.setPadding(new Insets(0, 10, 0, 0));
        if (mins > 0) timeLabel.setStyle("-fx-text-fill: #E06666; -fx-font-weight: bold; -fx-font-size: 13px;");
        else timeLabel.setStyle("-fx-text-fill: #858585; -fx-font-weight: bold; -fx-font-size: 13px;");

        ComboBox<TaskItem.CustomPriority> prioBox = new ComboBox<>();
        prioBox.getItems().addAll(appStats.getCustomPriorities());
        prioBox.setValue(task.getPriority());
        setupPriorityBoxColors(prioBox);
        prioBox.setOnAction(e -> {
            task.setPriority(prioBox.getValue());
            if (prioBox.getValue() != null && prioBox.getValue().getColorHex() != null) prioRect.setFill(Color.web(prioBox.getValue().getColorHex()));
            StorageManager.saveTasks(globalDatabase);
            if (sortComboBox.getValue().contains("Priority")) refreshList();
        });

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isFinished());
        checkBox.setOnAction(e -> {
            task.setFinished(checkBox.isSelected());
            if (checkBox.isSelected()) for (TaskItem.SubTask sub : task.getSubTasks()) sub.setFinished(true);
            StorageManager.saveTasks(globalDatabase);
            refreshList();
        });

        mainRow.getChildren().addAll(metaBox, textContainer, timeLabel, prioBox, checkBox);
        attachContextMenu(mainRow, task);

        // Sub-tasks
        VBox subTaskBox = new VBox(8);
        subTaskBox.setPadding(new Insets(0, 10, 15, 60));
        subTaskBox.setVisible(task.isExpanded() && !task.getSubTasks().isEmpty());
        subTaskBox.setManaged(task.isExpanded() && !task.getSubTasks().isEmpty());

        for (TaskItem.SubTask sub : task.getSubTasks()) {
            HBox subRow = new HBox(10);
            subRow.setAlignment(Pos.CENTER_LEFT);

            CheckBox subCheck = new CheckBox();
            subCheck.setSelected(sub.isFinished());
            subCheck.setOnAction(e -> { sub.setFinished(subCheck.isSelected()); StorageManager.saveTasks(globalDatabase); refreshList(); });

            Label subText = new Label(sub.getTextContent());
            subText.setWrapText(true);
            int subSize = Math.max(10, appStats.getTaskFontSize() - 2);
            String subFontStyle = "-fx-font-size: " + subSize + "px; ";
            if (sub.isFinished()) subText.setStyle(subFontStyle + "-fx-strikethrough: true; -fx-text-fill: #858585;");
            else subText.setStyle(subFontStyle + "-fx-strikethrough: false; -fx-text-fill: #CCCCCC;");

            HBox subTextContainer = new HBox(subText);
            subTextContainer.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(subTextContainer, Priority.ALWAYS);

            Button delSubBtn = new Button("❌");
            delSubBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF6666; -fx-cursor: hand; -fx-padding: 0;");
            delSubBtn.setOnAction(e -> { task.getSubTasks().remove(sub); StorageManager.saveTasks(globalDatabase); refreshList(); });

            subRow.getChildren().addAll(subCheck, subTextContainer, delSubBtn);
            subTaskBox.getChildren().add(subRow);
        }

        completeRow.getChildren().addAll(mainRow, subTaskBox);
        return completeRow;
    }

    private void reorderTasks(String draggedId, String targetId) {
        if (draggedId.equals(targetId)) return;

        TaskItem draggedTask = null;
        TaskItem targetTask = null;

        for (TaskItem task : globalDatabase) {
            if (task.getId().equals(draggedId)) draggedTask = task;
            if (task.getId().equals(targetId)) targetTask = task;
        }

        if (draggedTask != null && targetTask != null) {
            int draggedIdx = globalDatabase.indexOf(draggedTask);
            int targetIdx = globalDatabase.indexOf(targetTask);

            // Mathematical shifting correction
            globalDatabase.remove(draggedIdx);
            if (draggedIdx < targetIdx) {
                targetIdx--;
            }

            globalDatabase.add(targetIdx, draggedTask);
            StorageManager.saveTasks(globalDatabase);

            // Switch to custom order so the user instantly sees the result of their drag
            sortComboBox.setValue("Custom Order");
            refreshList();
        }
    }

    private void attachContextMenu(HBox row, TaskItem task) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem toggleFav = new MenuItem(task.isFavorite() ? "Remove Favorite" : "Add Favorite");
        toggleFav.setOnAction(e -> { task.setFavorite(!task.isFavorite()); StorageManager.saveTasks(globalDatabase); refreshList(); });

        MenuItem editItem = new MenuItem(appStats.getEditMenuText());
        editItem.setOnAction(e -> showEditDialog(task));

        MenuItem addSubItem = new MenuItem("Add Sub-task");
        addSubItem.setOnAction(e -> showAddSubTaskDialog(task));

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

        contextMenu.getItems().addAll(toggleFav, editItem, addSubItem, colorMenu, new SeparatorMenuItem(), archiveItem, deleteItem);
        row.setOnContextMenuRequested(e -> contextMenu.show(row, e.getScreenX(), e.getScreenY()));
    }

    private void showAddSubTaskDialog(TaskItem task) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Sub-task");
        dialog.setHeaderText("Create a new sub-task for: " + task.getTextContent());
        dialog.setContentText("Sub-task:");

        dialog.showAndWait().ifPresent(text -> {
            if (!text.trim().isEmpty()) {
                task.getSubTasks().add(new TaskItem.SubTask(text.trim()));
                task.setExpanded(true);
                StorageManager.saveTasks(globalDatabase);
                refreshList();
            }
        });
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
        if (moduleType == TaskItem.OriginModule.WORK) newTask.setWorkType("General");
        globalDatabase.add(newTask);

        if (sortComboBox.getValue().equals("Most Recent")) refreshList();
        else { sortComboBox.setValue("Most Recent"); refreshList(); }

        inputField.clear();
        StorageManager.saveTasks(globalDatabase);
    }

    // --- NEW: HTML Analytics Export Generator ---
    private void exportWorkAnalytics() {
        try {
            File exportFile = new File(System.getProperty("user.home") + "/Desktop/Studio_Work_Analytics.html");
            FileWriter writer = new FileWriter(exportFile);

            int totalTasks = 0;
            int completedTasks = 0;
            int totalTimeSeconds = 0;

            Map<String, Integer> workTypeTimeMap = new HashMap<>();
            Map<String, Integer> priorityCountMap = new HashMap<>();
            Map<String, String> priorityColorMap = new HashMap<>();

            // Calculate historical data from ALL Work tasks (including archived)
            for (TaskItem task : globalDatabase) {
                if (task.getOriginModule() == TaskItem.OriginModule.WORK) {
                    totalTasks++;
                    if (task.isFinished()) completedTasks++;
                    totalTimeSeconds += task.getTimeSpentSeconds();

                    String wt = task.getWorkType() == null || task.getWorkType().trim().isEmpty() ? "General" : task.getWorkType();
                    workTypeTimeMap.put(wt, workTypeTimeMap.getOrDefault(wt, 0) + task.getTimeSpentSeconds());

                    String prioName = task.getPriority() != null ? task.getPriority().getName() : "Unassigned";
                    String prioColor = task.getPriority() != null ? task.getPriority().getColorHex() : "#888888";
                    priorityCountMap.put(prioName, priorityCountMap.getOrDefault(prioName, 0) + 1);
                    priorityColorMap.put(prioName, prioColor);
                }
            }

            StringBuilder wtLabels = new StringBuilder();
            StringBuilder wtData = new StringBuilder();
            for (Map.Entry<String, Integer> entry : workTypeTimeMap.entrySet()) {
                wtLabels.append("'").append(entry.getKey()).append("',");
                wtData.append(entry.getValue() / 60).append(","); // Convert to minutes
            }

            StringBuilder pLabels = new StringBuilder();
            StringBuilder pData = new StringBuilder();
            StringBuilder pColors = new StringBuilder();
            for (Map.Entry<String, Integer> entry : priorityCountMap.entrySet()) {
                pLabels.append("'").append(entry.getKey()).append("',");
                pData.append(entry.getValue()).append(",");
                pColors.append("'").append(priorityColorMap.get(entry.getKey())).append("',");
            }

            int hours = totalTimeSeconds / 3600;
            int mins = (totalTimeSeconds % 3600) / 60;
            String timeStr = hours + "h " + mins + "m";
            double completionRate = totalTasks == 0 ? 0 : ((double) completedTasks / totalTasks) * 100;

            String htmlContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Studio Work Analytics</title>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { background-color: #1E1E1E; color: #E0E0E0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 40px; margin: 0; }
                    .header { text-align: center; margin-bottom: 40px; border-bottom: 2px solid #3E3E42; padding-bottom: 20px; }
                    h1 { color: #569CD6; margin: 0; font-size: 36px; }
                    .stats-container { display: flex; justify-content: space-around; margin-bottom: 40px; }
                    .stat-box { background-color: #2D2D30; padding: 20px; border-radius: 10px; width: 30%%; text-align: center; box-shadow: 0 4px 6px rgba(0,0,0,0.3); border: 1px solid #3E3E42; }
                    .stat-box h3 { margin: 0 0 10px 0; color: #AAAAAA; font-size: 16px; }
                    .stat-box p { margin: 0; font-size: 32px; font-weight: bold; color: #4EC9B0; }
                    .charts-wrapper { display: flex; justify-content: space-between; gap: 20px; }
                    .chart-container { background-color: #2D2D30; padding: 30px; border-radius: 10px; width: 48%%; box-shadow: 0 4px 6px rgba(0,0,0,0.3); border: 1px solid #3E3E42;}
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Studio Work Analytics</h1>
                    <p style="color: #AAAAAA;">Generated on %s</p>
                </div>
                
                <div class="stats-container">
                    <div class="stat-box">
                        <h3>Lifetime Completion Rate</h3>
                        <p style="color: #569CD6;">%s%%</p>
                    </div>
                    <div class="stat-box">
                        <h3>Total Tasks Handled</h3>
                        <p>%d Tasks</p>
                    </div>
                    <div class="stat-box">
                        <h3>Deep Focus Invested</h3>
                        <p style="color: #E06666;">%s</p>
                    </div>
                </div>

                <div class="charts-wrapper">
                    <div class="chart-container">
                        <canvas id="timeChart"></canvas>
                    </div>
                    <div class="chart-container">
                        <canvas id="priorityChart"></canvas>
                    </div>
                </div>

                <script>
                    Chart.defaults.color = '#AAAAAA';
                    Chart.defaults.font.family = "'Segoe UI', sans-serif";

                    // Chart 1: Time Spent per Work Type (Doughnut)
                    new Chart(document.getElementById('timeChart').getContext('2d'), {
                        type: 'doughnut',
                        data: {
                            labels: [%s],
                            datasets: [{
                                data: [%s],
                                backgroundColor: ['#4EC9B0', '#569CD6', '#E06666', '#FF8C00', '#C586C0'],
                                borderWidth: 0,
                                hoverOffset: 10
                            }]
                        },
                        options: {
                            responsive: true,
                            plugins: { 
                                title: { display: true, text: 'Development Time by Category (Minutes)', color: '#FFFFFF', font: { size: 18 } },
                                legend: { position: 'bottom' }
                            }
                        }
                    });

                    // Chart 2: Task Volume by Priority (Bar)
                    new Chart(document.getElementById('priorityChart').getContext('2d'), {
                        type: 'bar',
                        data: {
                            labels: [%s],
                            datasets: [{
                                label: 'Total Tasks Assigned',
                                data: [%s],
                                backgroundColor: [%s],
                                borderRadius: 5
                            }]
                        },
                        options: {
                            responsive: true,
                            plugins: { 
                                title: { display: true, text: 'Task Volume by Priority', color: '#FFFFFF', font: { size: 18 } },
                                legend: { display: false }
                            },
                            scales: { y: { beginAtZero: true, grid: { color: '#3E3E42' } }, x: { grid: { display: false } } }
                        }
                    });
                </script>
            </body>
            </html>
            """.formatted(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    String.format("%.1f", completionRate),
                    totalTasks,
                    timeStr,
                    wtLabels.toString(),
                    wtData.toString(),
                    pLabels.toString(),
                    pData.toString(),
                    pColors.toString()
            );

            writer.write(htmlContent);
            writer.flush();
            writer.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Work Analytics Exported to Desktop:\nStudio_Work_Analytics.html");
            alert.setHeaderText("Export Successful");
            alert.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to export report.");
            error.show();
        }
    }
}