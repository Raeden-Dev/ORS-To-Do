package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.TaskDialogs;
import com.raeden.ors_to_do.modules.dependencies.TaskCard;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.*;

public class DynamicModule extends BorderPane {
    private VBox listContainer;
    private AppStats.SectionConfig config;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private Runnable syncCallback;

    private TextField inputField;
    private TextField prefixField;
    private ComboBox<TaskItem.CustomPriority> priorityBox;

    private Label availableTasksLabel;
    private Label scoreLabel;
    private FlowPane filterContainer;
    private String activeFilter = "All";
    private ComboBox<String> sortComboBox;

    private List<Timeline> activeTimelines = new ArrayList<>();

    public DynamicModule(AppStats.SectionConfig config, List<TaskItem> globalDatabase, AppStats appStats, Runnable syncCallback) {
        this.config = config;
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        this.syncCallback = syncCallback;
        setPadding(new Insets(15));

        // ==========================================
        // --- TOP: Dynamic Header & Tracking ---
        // ==========================================
        VBox topArea = new VBox(10);

        HBox dashboardStrip = new HBox(15);
        dashboardStrip.setAlignment(Pos.CENTER_LEFT);
        dashboardStrip.setPadding(new Insets(12, 15, 12, 15));
        dashboardStrip.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 8; -fx-background-radius: 8;");
        dashboardStrip.setMinHeight(65);

        availableTasksLabel = new Label();
        availableTasksLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #569CD6;");
        dashboardStrip.getChildren().add(availableTasksLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        dashboardStrip.getChildren().add(headerSpacer);

        HBox badgesBox = new HBox(10);
        badgesBox.setAlignment(Pos.CENTER_RIGHT);

        if (config.isEnableScore()) {
            scoreLabel = new Label();
            scoreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-background-color: #332B00; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #FFD700; -fx-border-radius: 15;");
            badgesBox.getChildren().add(scoreLabel);
        }

        if (config.isHasStreak()) {
            Label streakLabel = new Label("🔥 " + appStats.getCurrentStreak() + " Day Streak");
            streakLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FF8C00; -fx-background-color: #331A00; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #FF8C00; -fx-border-radius: 15;");

            Label countdownLabel = new Label();
            countdownLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-background-color: #1E1E1E; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #555555; -fx-border-radius: 15;");

            Runnable updateClock = () -> {
                java.time.Duration duration = java.time.Duration.between(LocalTime.now(), LocalTime.MAX);
                countdownLabel.setText(String.format("Resets in: %02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
            };
            updateClock.run();

            Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock.run()));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
            activeTimelines.add(clock);

            badgesBox.getChildren().addAll(streakLabel, countdownLabel);
        }

        dashboardStrip.getChildren().add(badgesBox);

        if (config.isShowAnalytics()) {
            Button exportBtn = new Button("📊 Export");
            exportBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
            exportBtn.setOnAction(e -> com.raeden.ors_to_do.utils.AnalyticsExporter.exportSectionAnalytics(config, globalDatabase));
            dashboardStrip.getChildren().add(exportBtn);
        }

        topArea.getChildren().add(dashboardStrip);

        // ==========================================
        // --- REVERT: Filter & Sorting Row ---
        // ==========================================
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
        if (config.isShowPriority()) sortComboBox.getItems().addAll("Priority: Low to High", "Priority: High to Low");
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

        listContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);
        setCenter(scrollPane);

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
            TaskDialogs.setupPriorityBoxColors(priorityBox);
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

    private int getPriorityWeight(TaskItem.CustomPriority p) {
        if (p == null) return -1;
        int idx = appStats.getCustomPriorities().indexOf(p);
        return idx == -1 ? 999 : idx;
    }

    public void refreshList() {
        for (Timeline t : activeTimelines) t.stop();
        activeTimelines.clear();

        listContainer.getChildren().clear();
        int availableCount = 0;
        int completedCount = 0;

        Set<String> uniqueTags = new HashSet<>();
        List<TaskItem> tasksToDisplay = new ArrayList<>();

        for (TaskItem task : globalDatabase) {
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
            Runnable onUpdateTrigger = () -> {
                refreshList();
                if (syncCallback != null) syncCallback.run();
            };

            for (TaskItem task : tasksToDisplay) {
                listContainer.getChildren().add(new TaskCard(
                        task, config, appStats, globalDatabase, onUpdateTrigger, activeTimelines, this::reorderTasks
                ));
            }
        }

        if (config.isHasStreak()) availableTasksLabel.setText(config.getName() + " (" + completedCount + "/" + (availableCount + completedCount) + ")");
        else availableTasksLabel.setText("Active Tasks: " + availableCount);

        if (scoreLabel != null) scoreLabel.setText("🏆 Score: " + appStats.getGlobalScore());

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
}