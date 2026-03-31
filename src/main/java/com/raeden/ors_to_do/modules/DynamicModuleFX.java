package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DynamicModuleFX extends BorderPane {
    private VBox listContainer;
    private AppStats.SectionConfig config;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private Runnable syncCallback;

    // Inputs
    private TextField inputField;
    private TextField prefixField;
    private ComboBox<TaskItem.CustomPriority> priorityBox;

    // Headers & Tracking
    private Label availableTasksLabel;
    private FlowPane filterContainer;
    private String activeFilter = "All";
    private ComboBox<String> sortComboBox;

    private final String[] DARK_PASTELS = {
            "#2C3E50", "#34495E", "#1A252C", "#2D3748", "#2A4365",
            "#2C5282", "#2B6CB0", "#234E52", "#285E61", "#2C7A7B",
            "#22543D", "#276749", "#2F855A", "#744210", "#975A16",
            "#702459", "#97266D", "#44337A", "#553C9A", "#1A202C"
    };

    public DynamicModuleFX(AppStats.SectionConfig config, List<TaskItem> globalDatabase, AppStats appStats, Runnable syncCallback) {
        this.config = config;
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        this.syncCallback = syncCallback;
        setPadding(new Insets(15));

        // ==========================================
        // --- TOP: Dynamic Header & Tracking ---
        // ==========================================
        VBox topArea = new VBox(10);

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 5, 0));

        availableTasksLabel = new Label();
        availableTasksLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #569CD6;");
        headerBox.getChildren().add(availableTasksLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        headerBox.getChildren().add(headerSpacer);

        // Conditional: Streak & Midnight Countdown
        if (config.isHasStreak()) {
            Label streakLabel = new Label("🔥 " + appStats.getCurrentStreak() + " Day Streak");
            streakLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF8C00;");

            Label countdownLabel = new Label();
            countdownLabel.setStyle("-fx-text-fill: #858585; -fx-font-family: 'Consolas', monospace; -fx-font-size: 14px;");
            Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                java.time.Duration duration = java.time.Duration.between(LocalTime.now(), LocalTime.MAX);
                countdownLabel.setText(String.format("Resets in: %02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
            }));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();

            headerBox.getChildren().addAll(streakLabel, countdownLabel);
        }

        // Conditional: Analytics Export
        if (config.isShowAnalytics()) {
            Button exportBtn = new Button("📊 Export Analytics");
            exportBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            exportBtn.setOnAction(e -> exportAnalytics());
            headerBox.getChildren().add(exportBtn);
        }

        topArea.getChildren().add(headerBox);

        // Sorting & Filters
        HBox filterSortRow = new HBox(10);
        filterSortRow.setAlignment(Pos.CENTER_LEFT);
        filterSortRow.setPadding(new Insets(0, 0, 10, 0));

        filterContainer = new FlowPane(5, 5);
        if (config.isShowTags()) {
            filterSortRow.getChildren().add(filterContainer);
        }

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Custom Order", "Most Recent", "Oldest First", "Alphabetical");
        if (config.isShowPriority()) {
            sortComboBox.getItems().addAll("Priority: Low to High", "Priority: High to Low");
        }
        sortComboBox.setValue("Custom Order");

        sortComboBox.setStyle("-fx-background-color: #E0E0E0; -fx-cursor: hand;");

        sortComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); }
            }
        });

        sortComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-text-fill: black;"); }
            }
        });

        sortComboBox.setOnAction(e -> refreshList());

        filterSortRow.getChildren().addAll(filterSpacer, sortComboBox);
        topArea.getChildren().add(filterSortRow);

        setTop(topArea);

        // ==========================================
        // --- CENTER: Dynamic List ---
        // ==========================================
        listContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);
        setCenter(scrollPane);

        // ==========================================
        // --- BOTTOM: Dynamic Input Area ---
        // ==========================================
        HBox inputPanel = new HBox(10);
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.setPadding(new Insets(15, 0, 0, 0));

        if (config.isShowPrefix()) {
            prefixField = new TextField();
            prefixField.setPromptText("[PREFIX]");
            prefixField.setPrefWidth(80);
            prefixField.getStyleClass().add("input-field");
            inputPanel.getChildren().add(prefixField);
        }

        inputField = new TextField();
        inputField.setPromptText("Enter new task for " + config.getName() + "...");
        inputField.getStyleClass().add("input-field");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputPanel.getChildren().add(inputField);

        if (config.isShowPriority()) {
            priorityBox = new ComboBox<>();
            priorityBox.getItems().addAll(appStats.getCustomPriorities());
            if (!appStats.getCustomPriorities().isEmpty()) priorityBox.setValue(appStats.getCustomPriorities().get(1));
            setupPriorityBoxColors(priorityBox);
            inputPanel.getChildren().add(priorityBox);
        }

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-btn");
        Button clearBtn = new Button("Clear");

        inputPanel.getChildren().addAll(addBtn, clearBtn);

        addBtn.setOnAction(e -> addTask());
        inputField.setOnAction(e -> addTask());
        clearBtn.setOnAction(e -> {
            inputField.clear();
            if (prefixField != null) prefixField.clear();
        });

        setBottom(inputPanel);
        refreshList();
    }

    // --- Helper Methods & Refresh Logic ---

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
        int completedCount = 0;

        Set<String> uniqueTags = new HashSet<>();
        List<TaskItem> tasksToDisplay = new ArrayList<>();

        for (TaskItem task : globalDatabase) {
            // Check if task belongs to THIS dynamic section
            if (task.getSectionId() != null && task.getSectionId().equals(config.getId()) && !task.isArchived()) {

                String tag = null;
                if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty()) tag = task.getWorkType();
                else if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) tag = task.getPrefix();

                if (tag != null) uniqueTags.add(tag);

                boolean passesFilter = activeFilter.equals("All") || (tag != null && tag.equals(activeFilter));

                if (passesFilter) {
                    tasksToDisplay.add(task);
                    if (!task.isFinished()) availableCount++;
                    else completedCount++;
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

        // Header Update
        if (config.isHasStreak()) {
            availableTasksLabel.setText(config.getName() + " (" + completedCount + "/" + (availableCount + completedCount) + ")");
        } else {
            availableTasksLabel.setText("Active Tasks: " + availableCount);
        }

        if (config.isShowTags()) updateFilterPills(uniqueTags);
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

    // ==========================================
    // --- ROW RENDERER (The Chameleon) ---
    // ==========================================
    private VBox createTaskRow(TaskItem task) {
        VBox completeRow = new VBox();
        completeRow.getStyleClass().add("task-row");

        // --- FIXED: Apply Custom Background Color & Golden Highlight Border ---
        String bgStyle = task.getColorHex() != null ? "-fx-background-color: " + task.getColorHex() + "; " : "";
        String borderStyle = (config.isAllowFavorite() && task.isFavorite()) ? "-fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 4; " : "";
        String originalStyle = bgStyle + borderStyle;
        completeRow.setStyle(originalStyle);

        // Native Drag & Drop Logic
        completeRow.setOnDragDetected(event -> {
            Dragboard db = completeRow.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getId());
            db.setContent(content);
            event.consume();
        });
        completeRow.setOnDragOver(event -> {
            if (event.getGestureSource() != completeRow && event.getDragboard().hasString()) event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
        completeRow.setOnDragEntered(event -> {
            if (event.getGestureSource() != completeRow && event.getDragboard().hasString()) {
                completeRow.setStyle(originalStyle + " -fx-border-color: #569CD6; -fx-border-width: 2;");
            }
        });
        completeRow.setOnDragExited(event -> completeRow.setStyle(originalStyle));
        completeRow.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                reorderTasks(db.getString(), task.getId());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10));

        // Subtask Expand Button
        Button expandBtn = new Button(task.isExpanded() ? "▼" : "▶");
        expandBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-padding: 0 5 0 0; -fx-cursor: hand;");
        if (!config.isEnableSubTasks() || task.getSubTasks().isEmpty()) { expandBtn.setVisible(false); expandBtn.setManaged(false); }
        expandBtn.setOnAction(e -> { task.setExpanded(!task.isExpanded()); StorageManager.saveTasks(globalDatabase); refreshList(); });

        // Side Rectangle (Priority or Prefix Matched)
        Rectangle sideRect = new Rectangle(5, 25);
        sideRect.setArcWidth(3); sideRect.setArcHeight(3);
        if (config.isShowPriority() && task.getPriority() != null && task.getPriority().getColorHex() != null) {
            sideRect.setFill(Color.web(task.getPriority().getColorHex()));
        } else if (config.isShowPrefix() && appStats.isMatchDailyRectColor() && task.getPrefixColor() != null) {
            sideRect.setFill(Color.web(task.getPrefixColor()));
        } else {
            sideRect.setFill(Color.WHITE);
        }

        HBox metaBox = new HBox(7, expandBtn, sideRect);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        // --- NEW: Inject Golden Star if favorited ---
        if (config.isAllowFavorite() && task.isFavorite()) {
            Label starLabel = new Label("[⭐]");
            starLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: " + appStats.getTaskFontSize() + "px; -fx-font-weight: bold;");
            metaBox.getChildren().add(starLabel);
        }

        if (config.isShowDate()) {
            Label dateLabel = new Label("[" + task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "]");
            dateLabel.getStyleClass().add("task-metadata");
            metaBox.getChildren().add(dateLabel);
        }

        if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) {
            Label prefixLabel = new Label(task.getPrefix());
            prefixLabel.getStyleClass().add("task-prefix");
            String pColor = task.getPrefixColor() != null ? task.getPrefixColor() : "#4EC9B0";
            prefixLabel.setStyle("-fx-text-fill: " + pColor + "; -fx-font-size: " + appStats.getTaskFontSize() + "px;");
            metaBox.getChildren().add(prefixLabel);
        }

        if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty()) {
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

        // Track Time
        Label timeLabel = null;
        if (config.isTrackTime()) {
            int mins = task.getTimeSpentSeconds() / 60;
            timeLabel = new Label("⏱ " + mins + "m");
            timeLabel.setPadding(new Insets(0, 10, 0, 0));
            if (mins > 0) timeLabel.setStyle("-fx-text-fill: #E06666; -fx-font-weight: bold; -fx-font-size: 13px;");
            else timeLabel.setStyle("-fx-text-fill: #858585; -fx-font-weight: bold; -fx-font-size: 13px;");
        }

        // Priority Box
        ComboBox<TaskItem.CustomPriority> prioBox = null;
        if (config.isShowPriority()) {
            ComboBox<TaskItem.CustomPriority> localPrioBox = new ComboBox<>();
            prioBox = localPrioBox;

            localPrioBox.getItems().addAll(appStats.getCustomPriorities());
            localPrioBox.setValue(task.getPriority());
            setupPriorityBoxColors(localPrioBox);

            localPrioBox.setOnAction(e -> {
                task.setPriority(localPrioBox.getValue());
                StorageManager.saveTasks(globalDatabase);
                if (sortComboBox.getValue().contains("Priority")) refreshList();
            });
        }

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isFinished());
        checkBox.setOnAction(e -> {
            task.setFinished(checkBox.isSelected());
            if (checkBox.isSelected()) for (TaskItem.SubTask sub : task.getSubTasks()) sub.setFinished(true);
            StorageManager.saveTasks(globalDatabase);
            refreshList();
            if (syncCallback != null) syncCallback.run(); // Updates Sidebar/FocusHub
        });

        mainRow.getChildren().addAll(metaBox, textContainer);
        if (timeLabel != null) mainRow.getChildren().add(timeLabel);
        if (prioBox != null) mainRow.getChildren().add(prioBox);
        mainRow.getChildren().add(checkBox);

        attachContextMenu(mainRow, task);

        // Sub-tasks Rendering
        VBox subTaskBox = new VBox(8);
        if (config.isEnableSubTasks() && !task.getSubTasks().isEmpty()) {
            subTaskBox.setPadding(new Insets(0, 10, 15, 60));
            subTaskBox.setVisible(task.isExpanded());
            subTaskBox.setManaged(task.isExpanded());

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
        }

        completeRow.getChildren().addAll(mainRow, subTaskBox);
        return completeRow;
    }

    private void reorderTasks(String draggedId, String targetId) {
        if (draggedId.equals(targetId)) return;
        TaskItem draggedTask = null, targetTask = null;
        for (TaskItem task : globalDatabase) {
            if (task.getId().equals(draggedId)) draggedTask = task;
            if (task.getId().equals(targetId)) targetTask = task;
        }
        if (draggedTask != null && targetTask != null) {
            int draggedIdx = globalDatabase.indexOf(draggedTask);
            int targetIdx = globalDatabase.indexOf(targetTask);
            globalDatabase.remove(draggedIdx);
            if (draggedIdx < targetIdx) targetIdx--;
            globalDatabase.add(targetIdx, draggedTask);
            StorageManager.saveTasks(globalDatabase);
            sortComboBox.setValue("Custom Order");
            refreshList();
        }
    }

    private void attachContextMenu(HBox row, TaskItem task) {
        ContextMenu contextMenu = new ContextMenu();

        // --- NEW: Dynamic Context Menu Favorite Option ---
        if (config.isAllowFavorite()) {
            MenuItem favItem = new MenuItem(task.isFavorite() ? "Remove Favorite" : "Mark as Favorite");
            favItem.setOnAction(e -> {
                task.setFavorite(!task.isFavorite());
                StorageManager.saveTasks(globalDatabase);
                refreshList();
            });
            contextMenu.getItems().addAll(favItem, new SeparatorMenuItem());
        }

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

        MenuItem deleteItem = new MenuItem(appStats.getDeleteMenuText());
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> { globalDatabase.remove(task); StorageManager.saveTasks(globalDatabase); refreshList(); });

        contextMenu.getItems().addAll(editItem);
        if (config.isEnableSubTasks()) {
            MenuItem addSubItem = new MenuItem("Add Sub-task");
            addSubItem.setOnAction(e -> showAddSubTaskDialog(task));
            contextMenu.getItems().add(addSubItem);
        }
        contextMenu.getItems().add(colorMenu);

        if (config.isAllowArchive()) {
            MenuItem archiveItem = new MenuItem(appStats.getArchiveMenuText());
            archiveItem.setOnAction(e -> {
                if(!task.isFinished()) task.setFinished(true);
                task.setArchived(true);
                StorageManager.saveTasks(globalDatabase); refreshList();
            });
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(archiveItem);
        }

        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(deleteItem);

        row.setOnContextMenuRequested(e -> contextMenu.show(row, e.getScreenX(), e.getScreenY()));
    }

    private void showAddSubTaskDialog(TaskItem task) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Sub-task");
        dialog.setHeaderText("Create a new sub-task for: " + task.getTextContent());
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
        grid.add(new Label("Content:"), 0, 0); grid.add(contentField, 1, 0);

        TextField prefixFieldEdit = null;
        ColorPicker preC = null;
        if (config.isShowPrefix()) {
            prefixFieldEdit = new TextField(task.getPrefix());
            preC = new ColorPicker(Color.web(task.getPrefixColor() != null ? task.getPrefixColor() : "#4EC9B0"));
            grid.add(new Label("Prefix:"), 0, 1); grid.add(prefixFieldEdit, 1, 1);
            grid.add(new Label("Prefix Color:"), 0, 2); grid.add(preC, 1, 2);
        }

        ComboBox<TaskItem.CustomPriority> prioBoxEdit = null;
        if (config.isShowPriority()) {
            prioBoxEdit = new ComboBox<>();
            prioBoxEdit.getItems().addAll(appStats.getCustomPriorities());
            prioBoxEdit.setValue(task.getPriority());
            setupPriorityBoxColors(prioBoxEdit);
            grid.add(new Label("Priority:"), 0, 3); grid.add(prioBoxEdit, 1, 3);
        }

        TextField workTypeField = null;
        if (config.isShowWorkType()) {
            workTypeField = new TextField(task.getWorkType());
            grid.add(new Label("Work Type:"), 0, 4); grid.add(workTypeField, 1, 4);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField finalPrefixFieldEdit = prefixFieldEdit;
        ColorPicker finalPreC = preC;
        ComboBox<TaskItem.CustomPriority> finalPrioBoxEdit = prioBoxEdit;
        TextField finalWorkTypeField = workTypeField;

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                task.setTextContent(contentField.getText().trim());
                if (config.isShowPrefix() && finalPrefixFieldEdit != null) {
                    task.setPrefix(finalPrefixFieldEdit.getText().trim());
                    task.setPrefixColor(String.format("#%02X%02X%02X", (int)(finalPreC.getValue().getRed()*255), (int)(finalPreC.getValue().getGreen()*255), (int)(finalPreC.getValue().getBlue()*255)));
                }
                if (config.isShowPriority() && finalPrioBoxEdit != null) task.setPriority(finalPrioBoxEdit.getValue());
                if (config.isShowWorkType() && finalWorkTypeField != null) task.setWorkType(finalWorkTypeField.getText().trim());

                StorageManager.saveTasks(globalDatabase);
                refreshList();
            }
        });
    }

    private void addTask() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        TaskItem.CustomPriority defaultPrio = null;
        if (config.isShowPriority() && priorityBox != null) defaultPrio = priorityBox.getValue();
        else if (config.isShowPriority() && !appStats.getCustomPriorities().isEmpty()) defaultPrio = appStats.getCustomPriorities().get(0);

        TaskItem newTask = new TaskItem(text, defaultPrio, config.getId());

        if (config.isShowPrefix() && prefixField != null) {
            String pText = prefixField.getText().trim();
            if (!pText.isEmpty()) {
                if (!pText.startsWith("[")) pText = "[" + pText;
                if (!pText.endsWith("]")) pText = pText + "]";
                newTask.setPrefix(pText.toUpperCase());
                newTask.setPrefixColor("#4EC9B0");
            }
        }
        if (config.isShowWorkType()) newTask.setWorkType("General");

        globalDatabase.add(newTask);

        if (sortComboBox.getValue().equals("Most Recent")) refreshList();
        else { sortComboBox.setValue("Most Recent"); refreshList(); }

        inputField.clear();
        if (prefixField != null) prefixField.clear();
        StorageManager.saveTasks(globalDatabase);
    }

    // --- Dynamic Analytics Builder ---
    private void exportAnalytics() {
        try {
            File exportFile = new File(System.getProperty("user.home") + "/Desktop/" + config.getName().replaceAll(" ", "_") + "_Analytics.html");
            FileWriter writer = new FileWriter(exportFile);

            int totalTasks = 0;
            int completedTasks = 0;
            int totalTimeSeconds = 0;

            Map<String, Integer> categoryMap = new HashMap<>();

            for (TaskItem task : globalDatabase) {
                if (task.getSectionId() != null && task.getSectionId().equals(config.getId())) {
                    totalTasks++;
                    if (task.isFinished()) completedTasks++;
                    totalTimeSeconds += task.getTimeSpentSeconds();

                    String key = "Uncategorized";
                    if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty()) key = task.getWorkType();
                    else if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) key = task.getPrefix();
                    else if (config.isShowPriority() && task.getPriority() != null) key = task.getPriority().getName();

                    // If tracking time, chart is Time-Based. If not, chart is Volume-Based.
                    int valueToAdd = config.isTrackTime() ? task.getTimeSpentSeconds() / 60 : 1;
                    categoryMap.put(key, categoryMap.getOrDefault(key, 0) + valueToAdd);
                }
            }

            StringBuilder labels = new StringBuilder();
            StringBuilder data = new StringBuilder();
            for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                labels.append("'").append(entry.getKey()).append("',");
                data.append(entry.getValue()).append(",");
            }

            double completionRate = totalTasks == 0 ? 0 : ((double) completedTasks / totalTasks) * 100;
            String chartLabel = config.isTrackTime() ? "Time Spent (Minutes)" : "Task Volume";

            String htmlContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s Analytics</title>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { background-color: #1E1E1E; color: #E0E0E0; font-family: 'Segoe UI', sans-serif; padding: 40px; }
                    .header { text-align: center; margin-bottom: 40px; border-bottom: 2px solid #3E3E42; padding-bottom: 20px; }
                    h1 { color: #569CD6; }
                    .stats-container { display: flex; justify-content: space-around; margin-bottom: 40px; }
                    .stat-box { background-color: #2D2D30; padding: 20px; border-radius: 10px; width: 30%%; text-align: center; border: 1px solid #3E3E42; }
                    .stat-box p { font-size: 32px; font-weight: bold; color: #4EC9B0; margin:0;}
                    .chart-container { background-color: #2D2D30; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.3); border: 1px solid #3E3E42;}
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>%s Dashboard</h1>
                    <p style="color: #AAAAAA;">Generated on %s</p>
                </div>
                
                <div class="stats-container">
                    <div class="stat-box"><h3>Completion Rate</h3><p>%s%%</p></div>
                    <div class="stat-box"><h3>Total Tasks</h3><p>%d</p></div>
                    <div class="stat-box"><h3>Total Time Tracked</h3><p>%dh %dm</p></div>
                </div>

                <div class="chart-container">
                    <canvas id="mainChart" height="100"></canvas>
                </div>

                <script>
                    Chart.defaults.color = '#AAAAAA';
                    new Chart(document.getElementById('mainChart').getContext('2d'), {
                        type: 'bar',
                        data: {
                            labels: [%s],
                            datasets: [{
                                label: '%s',
                                data: [%s],
                                backgroundColor: ['#4EC9B0', '#569CD6', '#E06666', '#FF8C00', '#C586C0'],
                                borderRadius: 5
                            }]
                        }
                    });
                </script>
            </body>
            </html>
            """.formatted(
                    config.getName(), config.getName(), LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    String.format("%.1f", completionRate), totalTasks,
                    (totalTimeSeconds / 3600), ((totalTimeSeconds % 3600) / 60),
                    labels.toString(), chartLabel, data.toString()
            );

            writer.write(htmlContent);
            writer.flush();
            writer.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Analytics Exported to Desktop:\n" + exportFile.getName());
            alert.setHeaderText("Export Successful");
            alert.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}