package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.AnalyticsExporter;
import com.raeden.ors_to_do.modules.dependencies.TaskDialogs;
import com.raeden.ors_to_do.modules.dependencies.TaskCard;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.*;

public class DynamicModule extends StackPane {

    private BorderPane mainContent;
    private VBox zenOverlay;
    private boolean isZenMode = false;
    private TaskItem currentZenTask = null;
    private Button zenModeBtn;

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

        mainContent = new BorderPane();
        mainContent.setPadding(new Insets(15));

        zenOverlay = new VBox(20);
        zenOverlay.setAlignment(Pos.CENTER);
        zenOverlay.setStyle("-fx-background-color: #1E1E1E;");
        zenOverlay.setVisible(false);

        getChildren().addAll(mainContent, zenOverlay);

        VBox topArea = new VBox(10);

        HBox dashboardStrip = new HBox(15);
        dashboardStrip.setAlignment(Pos.CENTER_LEFT);
        dashboardStrip.setPadding(new Insets(15));
        dashboardStrip.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 8; -fx-background-radius: 8;");

        availableTasksLabel = new Label();
        String titleColor = appStats.isMatchTitleColor() ? config.getSidebarColor() : "#569CD6";
        availableTasksLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");
        dashboardStrip.getChildren().add(availableTasksLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        dashboardStrip.getChildren().add(headerSpacer);

        // --- FIXED: Smart FlowPane wraps badges into a second line ONLY if they run out of room ---
        FlowPane badgesFlow = new FlowPane(10, 10);
        badgesFlow.setAlignment(Pos.CENTER_RIGHT);
        badgesFlow.setPrefWrapLength(400); // Hint to start wrapping if it drops below this pixel width

        if (config.isEnableScore() || config.isRewardsPage()) {
            scoreLabel = new Label();
            scoreLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-background-color: #332B00; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #FFD700; -fx-border-radius: 15;");
            badgesFlow.getChildren().add(scoreLabel);
        }

        if (config.isHasStreak()) {
            Label streakLabel = new Label("🔥 " + appStats.getCurrentStreak() + " Day Streak");
            streakLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF8C00; -fx-background-color: #331A00; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #FF8C00; -fx-border-radius: 15;");

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

            sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) clock.stop();
            });

            badgesFlow.getChildren().addAll(streakLabel, countdownLabel);
        }

        if (config.isEnableZenMode()) {
            zenModeBtn = new Button("☯ Zen Mode");
            zenModeBtn.setOnAction(e -> toggleZenMode());
            badgesFlow.getChildren().add(zenModeBtn);
        }

        if (config.isShowAnalytics()) {
            Button exportBtn = new Button("📊 Export");
            exportBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15; -fx-background-radius: 15; -fx-border-color: #569CD6; -fx-border-radius: 15; -fx-font-size: 13px;");
            exportBtn.setOnAction(e -> AnalyticsExporter.exportSectionAnalytics(config, globalDatabase));
            badgesFlow.getChildren().add(exportBtn);
        }

        dashboardStrip.getChildren().add(badgesFlow);
        topArea.getChildren().add(dashboardStrip);

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

        mainContent.setTop(topArea);

        listContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);
        mainContent.setCenter(scrollPane);

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
        inputField.setPromptText(config.isRewardsPage() ? "Enter new reward..." : "Enter new task for " + config.getName() + "...");
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

        mainContent.setBottom(inputPanel);
        refreshList();
    }

    private void toggleZenMode() {
        isZenMode = !isZenMode;

        if (getScene() != null && getScene().getRoot() instanceof BorderPane) {
            Node sidebar = ((BorderPane) getScene().getRoot()).getLeft();
            if (sidebar != null) {
                sidebar.setVisible(!isZenMode);
                sidebar.setManaged(!isZenMode);
            }
        }

        if (isZenMode) {
            mainContent.setVisible(false);
            zenOverlay.setVisible(true);
            refreshZenMode(false);
        } else {
            mainContent.setVisible(true);
            zenOverlay.setVisible(false);
            refreshList();
        }
    }

    private void refreshZenMode(boolean forceReroll) {
        zenOverlay.getChildren().clear();

        List<TaskItem> validTasks = new ArrayList<>();
        for (TaskItem t : globalDatabase) {
            if (t.getSectionId() != null && t.getSectionId().equals(config.getId()) && !t.isArchived() && !t.isFinished()) {
                boolean isLocked = false;
                if (t.getDependsOnTaskIds() != null && !t.getDependsOnTaskIds().isEmpty()) {
                    isLocked = globalDatabase.stream().anyMatch(dep -> t.getDependsOnTaskIds().contains(dep.getId()) && !dep.isFinished());
                }
                if (!isLocked) validTasks.add(t);
            }
        }

        if (validTasks.isEmpty()) {
            Label msg = new Label("All caught up! No tasks available for Zen Mode.");
            msg.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 24px; -fx-font-weight: bold;");
            Button exitBtn = new Button("Exit Zen Mode");
            exitBtn.setStyle("-fx-background-color: #569CD6; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
            exitBtn.setOnAction(e -> toggleZenMode());
            zenOverlay.getChildren().addAll(msg, exitBtn);
            return;
        }

        if (config.isShowPriority()) {
            int minWeight = validTasks.stream().mapToInt(t -> getPriorityWeight(t.getPriority())).min().orElse(999);
            validTasks.removeIf(t -> getPriorityWeight(t.getPriority()) > minWeight);
        }

        if (currentZenTask == null || currentZenTask.isFinished() || currentZenTask.isArchived() || forceReroll || !validTasks.contains(currentZenTask)) {
            currentZenTask = validTasks.get(new Random().nextInt(validTasks.size()));
        }

        Label zenHeader = new Label("☯ ZEN MODE");
        zenHeader.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #FF6666; -fx-effect: dropshadow(three-pass-box, #FF6666, 15, 0, 0, 0);");

        Label zenSub = new Label("Focus on this ONE task. Ignore everything else.");
        zenSub.setStyle("-fx-font-size: 18px; -fx-text-fill: #AAAAAA; -fx-padding: 0 0 40 0;");

        Runnable onZenUpdate = () -> {
            if (currentZenTask != null && currentZenTask.isFinished()) {
                // --- FIXED: Explicitly sets this alert as a child of the main window so it can NEVER hide behind it! ---
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Zen Mode");
                alert.setHeaderText(null);
                alert.setContentText("Task crushed! Great job.");
                TaskDialogs.styleDialog(alert);

                if (getScene() != null && getScene().getWindow() != null) {
                    alert.initOwner(getScene().getWindow());
                }

                alert.show();
                currentZenTask = null;
            }
            refreshZenMode(false);
            if (syncCallback != null) syncCallback.run();
        };

        TaskCard zenCard = new TaskCard(currentZenTask, config, appStats, globalDatabase, onZenUpdate, activeTimelines, this::reorderTasks);
        zenCard.setMaxWidth(800);

        HBox cardContainer = new HBox(zenCard);
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setPadding(new Insets(40, 0, 80, 0));

        Button rerollBtn = new Button("🎲 Reroll Task");
        rerollBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        rerollBtn.setOnAction(e -> refreshZenMode(true));

        Button exitBtn = new Button("❌ Exit Zen Mode");
        exitBtn.setStyle("-fx-background-color: #FF6666; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> toggleZenMode());

        HBox btnBox = new HBox(20, rerollBtn, exitBtn);
        btnBox.setAlignment(Pos.CENTER);

        zenOverlay.getChildren().addAll(zenHeader, zenSub, cardContainer, btnBox);
    }

    private int getPriorityWeight(TaskItem.CustomPriority p) {
        if (p == null) return -1;
        int idx = appStats.getCustomPriorities().indexOf(p);
        return idx == -1 ? 999 : idx;
    }

    public void refreshList() {
        for (Timeline t : activeTimelines) t.stop();
        activeTimelines.clear();

        if (isZenMode) {
            refreshZenMode(false);
            return;
        }

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

        if (config.isEnableZenMode() && zenModeBtn != null) {
            if (availableCount >= appStats.getZenModeThreshold()) {
                zenModeBtn.setDisable(false);
                zenModeBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF6666; -fx-background-color: #331A1A; -fx-padding: 5 15; -fx-background-radius: 15; -fx-border-color: #FF6666; -fx-border-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, #FF4444, 10, 0, 0, 0);");
                zenModeBtn.setText("☯ Zen Mode");
            } else {
                zenModeBtn.setDisable(true);
                zenModeBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555555; -fx-background-color: transparent; -fx-padding: 5 15; -fx-background-radius: 15; -fx-border-color: #3E3E42; -fx-border-radius: 15;");
                zenModeBtn.setText("☯ Zen Mode (" + availableCount + "/" + appStats.getZenModeThreshold() + ")");
            }
        }

        if (tasksToDisplay.isEmpty()) {
            Label emptyLabel = new Label(config.isRewardsPage() ? "Add a reward to your shop!" : "Add a task to get started!");
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
        else availableTasksLabel.setText((config.isRewardsPage() ? "Available Items: " : "Active Tasks: ") + availableCount);

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