package com.raeden.ors_to_do.modules.dependencies.ui.layout;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.OriginModule;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.services.SystemTrayManager;
import com.raeden.ors_to_do.modules.dependencies.services.GlobalActivityTracker;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class PomodoroTimer extends VBox {
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private Runnable refreshCallback;
    private Runnable onUrgeSurfing; // Trigger for the overlay

    private Timeline timeline;
    private int timeLeft = 25 * 60;
    private int lastTrackedTimeLeft = 25 * 60; // Tracks precise elapsed time to prevent double-counting
    private boolean isFocusMode = true;

    private Label timeDisplay;
    private Label statusLabel;
    private Button startPauseBtn;
    private ComboBox<Integer> timerOptions;
    private Button urgeBtn; // The new Urge Button

    private TextField taskSearchField;
    private ComboBox<TaskItem> taskSelector;
    private List<TaskItem> allFocusableTasks = new ArrayList<>();

    public PomodoroTimer(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback, Runnable onUrgeSurfing) {
        super(20); // Slightly increased spacing for aesthetics
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.refreshCallback = refreshCallback;
        this.onUrgeSurfing = onUrgeSurfing;

        setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(this, Priority.ALWAYS);
        setMinWidth(450);

        // --- NEW: Sleek Card Aesthetic ---
        setStyle("-fx-background-color: #252526; -fx-background-radius: 12; -fx-border-color: #3E3E42; -fx-border-width: 1; -fx-border-radius: 12; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 5);");

        statusLabel = new Label("FOCUS SESSION");
        statusLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #569CD6; -fx-letter-spacing: 2px;");
        statusLabel.getStyleClass().add("focus-status");
        VBox.setMargin(statusLabel, new Insets(10, 0, 0, 0));

        VBox linkBox = new VBox(8);
        linkBox.setAlignment(Pos.CENTER);
        Label linkLabel = new Label("Link Focus to Task:");
        linkLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 13px; -fx-font-weight: bold;");

        taskSearchField = new TextField();
        taskSearchField.setPromptText("🔍 Search tasks...");
        taskSearchField.setPrefWidth(320);
        taskSearchField.setMaxWidth(320);
        taskSearchField.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: white; -fx-border-color: #555555; -fx-border-radius: 5; -fx-padding: 8;");

        taskSearchField.textProperty().addListener((obs, oldText, newText) -> {
            applyTaskFilter(newText);
        });

        taskSelector = new ComboBox<>();
        taskSelector.setPrefWidth(320);
        taskSelector.setStyle("-fx-background-color: #3E3E42; -fx-cursor: hand; -fx-border-radius: 5; -fx-background-radius: 5;");

        taskSelector.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TaskItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("None (Free Focus)");
                } else {
                    setText("[" + getSectionName(item) + "] " + item.getTextContent());
                }
                setStyle("-fx-text-fill: black;");
            }
        });
        taskSelector.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TaskItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("None (Free Focus)");
                } else {
                    setText("[" + getSectionName(item) + "] " + item.getTextContent());
                }
                setStyle("-fx-text-fill: white;");
            }
        });

        linkBox.getChildren().addAll(linkLabel, taskSearchField, taskSelector);

        // --- NEW: Huge, styled time display ---
        timeDisplay = new Label("25:00");
        timeDisplay.setStyle("-fx-font-size: 85px; -fx-font-weight: bold; -fx-text-fill: #569CD6;"); // Blue for focus

        HBox optionsPanel = new HBox(10);
        optionsPanel.setAlignment(Pos.CENTER);
        Label focusLengthLabel = new Label("Focus Length (m): ");
        focusLengthLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px;");

        timerOptions = new ComboBox<>();
        timerOptions.getItems().addAll(1, 10, 25, 30, 40, 60, 90, 120);
        timerOptions.setValue(25);
        timerOptions.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: white; -fx-border-color: #555555; -fx-border-radius: 3; -fx-cursor: hand;");
        timerOptions.setOnAction(e -> {
            if (timeline == null || !timeline.getStatus().equals(Timeline.Status.RUNNING)) {
                if (isFocusMode) {
                    timeLeft = timerOptions.getValue() * 60;
                    lastTrackedTimeLeft = timeLeft;
                    updateDisplay();
                }
            }
        });
        optionsPanel.getChildren().addAll(focusLengthLabel, timerOptions);

        HBox btnPanel = new HBox(20);
        btnPanel.setAlignment(Pos.CENTER);

        startPauseBtn = new Button("START");
        startPauseBtn.setStyle("-fx-background-color: #569CD6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-font-size: 14px; -fx-background-radius: 5;");

        Button resetBtn = new Button("RESET");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #E06666; -fx-text-fill: #E06666; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 25; -fx-font-size: 14px; -fx-border-radius: 5;");

        btnPanel.getChildren().addAll(startPauseBtn, resetBtn);

        // --- NEW: Urge Surfing Button ---
        urgeBtn = new Button("🌊 Resist Urge (Breathe)");
        urgeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #4EC9B0; -fx-text-fill: #4EC9B0; -fx-border-radius: 5; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 10 25; -fx-font-size: 13px;");
        urgeBtn.setOnAction(e -> {
            // Auto pause the timer if running before doing the breathing exercise
            if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
                startPauseBtn.fire();
            }
            onUrgeSurfing.run();
        });

        getChildren().addAll(statusLabel, linkBox, timeDisplay, optionsPanel, btnPanel, new Separator(), urgeBtn);
        setupTimerLogic();

        startPauseBtn.setOnAction(e -> {
            if (timeline.getStatus() == Timeline.Status.RUNNING) {
                timeline.pause();

                // Track time on manual pause
                if (isFocusMode && taskSelector.getValue() != null) {
                    int elapsed = lastTrackedTimeLeft - timeLeft;
                    if (elapsed > 0) taskSelector.getValue().addTimeSpent(elapsed);
                    lastTrackedTimeLeft = timeLeft;
                    StorageManager.saveTasks(globalDatabase);
                    if (refreshCallback != null) refreshCallback.run();
                }

                startPauseBtn.setText("RESUME");
                startPauseBtn.setStyle("-fx-background-color: #FF8C00; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-font-size: 14px; -fx-background-radius: 5;");
            } else {
                GlobalActivityTracker.resetActivityTime();
                lastTrackedTimeLeft = timeLeft; // Sync tracker before un-pausing
                timeline.play();
                startPauseBtn.setText("PAUSE");
                startPauseBtn.setStyle("-fx-background-color: #569CD6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-font-size: 14px; -fx-background-radius: 5;");
            }
        });

        resetBtn.setOnAction(e -> resetTimer(isFocusMode));
    }

    public void refreshTasks() {
        // Toggle urge button dynamically based on settings
        if (urgeBtn != null) {
            urgeBtn.setVisible(appStats.isEnableUrgeButton());
            urgeBtn.setManaged(appStats.isEnableUrgeButton());
        }

        TaskItem selected = taskSelector.getValue();

        allFocusableTasks.clear();
        allFocusableTasks.add(null);

        for (TaskItem task : globalDatabase) {
            if (!task.isFinished() && !task.isArchived()) {
                boolean canFocus = false;
                if (task.getSectionId() != null) {
                    for (SectionConfig config : appStats.getSections()) {
                        if (config.getId().equals(task.getSectionId()) && config.isTrackTime()) {
                            canFocus = true;
                            break;
                        }
                    }
                } else if (task.getOriginModule() == OriginModule.WORK || task.getOriginModule() == OriginModule.QUICK) {
                    canFocus = true;
                }
                if (canFocus) allFocusableTasks.add(task);
            }
        }

        applyTaskFilter(taskSearchField.getText());

        if (selected != null && taskSelector.getItems().contains(selected)) {
            taskSelector.setValue(selected);
        } else {
            taskSelector.setValue(null);
        }
    }

    private void applyTaskFilter(String query) {
        TaskItem currentlySelected = taskSelector.getValue();
        taskSelector.getItems().clear();

        if (query == null || query.trim().isEmpty()) {
            taskSelector.getItems().addAll(allFocusableTasks);
        } else {
            String lowerQuery = query.toLowerCase();
            for (TaskItem task : allFocusableTasks) {
                if (task == null) {
                    taskSelector.getItems().add(null);
                } else {
                    String taskText = task.getTextContent().toLowerCase();
                    String secName = getSectionName(task).toLowerCase();

                    if (taskText.contains(lowerQuery) || secName.contains(lowerQuery)) {
                        taskSelector.getItems().add(task);
                    }
                }
            }
        }

        if (currentlySelected != null && taskSelector.getItems().contains(currentlySelected)) {
            taskSelector.setValue(currentlySelected);
        } else if (!taskSelector.getItems().isEmpty()) {
            taskSelector.setValue(taskSelector.getItems().get(0));
        }

        if (taskSearchField.isFocused() && !taskSelector.getItems().isEmpty()) {
            taskSelector.show();
        }
    }

    private String getSectionName(TaskItem item) {
        if (item == null) return "";
        if (item.getSectionId() != null) {
            for (SectionConfig c : appStats.getSections()) {
                if (c.getId().equals(item.getSectionId())) {
                    return c.getName();
                }
            }
        }
        return "Task";
    }

    private void setupTimerLogic() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

            // --- Global Inactivity Auto-Pause Check ---
            if (isFocusMode && appStats.getFocusInactivityThreshold() > 0) {
                long inactiveMillis = System.currentTimeMillis() - GlobalActivityTracker.getLastActivityTime();
                long thresholdMillis = appStats.getFocusInactivityThreshold() * 60 * 1000L;

                if (inactiveMillis > thresholdMillis) {
                    timeline.pause();

                    // Add time before pausing for inactivity
                    if (taskSelector.getValue() != null) {
                        int elapsed = lastTrackedTimeLeft - timeLeft;
                        if (elapsed > 0) taskSelector.getValue().addTimeSpent(elapsed);
                        lastTrackedTimeLeft = timeLeft;
                        StorageManager.saveTasks(globalDatabase);
                        if (refreshCallback != null) refreshCallback.run();
                    }

                    startPauseBtn.setText("RESUME");
                    startPauseBtn.setStyle("-fx-background-color: #FF8C00; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-font-size: 14px; -fx-background-radius: 5;");

                    SystemTrayManager.pushNotification("Focus Paused", "Timer paused due to " + appStats.getFocusInactivityThreshold() + " minutes of inactivity.");
                    GlobalActivityTracker.resetActivityTime();
                    return;
                }
            }

            timeLeft--; updateDisplay();

            // --- Mid-Session Timed Task Auto-Completion ---
            if (isFocusMode && taskSelector.getValue() != null) {
                TaskItem activeTask = taskSelector.getValue();
                if (activeTask.getTargetTimeMinutes() > 0 && !activeTask.isFinished()) {
                    int elapsedThisSession = lastTrackedTimeLeft - timeLeft;
                    int totalTracked = activeTask.getTimeSpentSeconds() + elapsedThisSession;

                    if (totalTracked >= activeTask.getTargetTimeMinutes() * 60) {

                        activeTask.addTimeSpent(elapsedThisSession);
                        lastTrackedTimeLeft = timeLeft;

                        SectionConfig taskConfig = appStats.getSections().stream().filter(c -> c.getId().equals(activeTask.getSectionId())).findFirst().orElse(null);
                        com.raeden.ors_to_do.modules.dependencies.ui.utils.TaskActionHandler.handleTaskCompletion(activeTask, taskConfig, appStats, globalDatabase, refreshCallback, null);

                        if (taskConfig != null && taskConfig.isEnableStatsSystem()) {
                            com.raeden.ors_to_do.modules.dependencies.ui.utils.TaskActionHandler.processRPGStats(activeTask, appStats, true);
                            StorageManager.saveStats(appStats);
                        }

                        SystemTrayManager.pushNotification("Timed Task Complete!", "Target focus time reached for: " + activeTask.getTextContent());

                        // Deselect task so timer defaults back to "Free Focus"
                        taskSelector.setValue(null);
                    }
                }
            }

            if (timeLeft <= 0) {
                timeline.pause();
                isFocusMode = !isFocusMode;
                resetTimer(isFocusMode);

                String title = isFocusMode ? "Pomodoro Break Over" : "Pomodoro Session Complete";
                String message = isFocusMode ? "Back to work!" : "Great job! Take a short break.";
                SystemTrayManager.pushNotification(title, message);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void resetTimer(boolean focus) {
        if (timeline != null) timeline.pause();

        if (this.isFocusMode && taskSelector.getValue() != null) {
            int elapsed = lastTrackedTimeLeft - timeLeft;
            if (elapsed > 0) {
                taskSelector.getValue().addTimeSpent(elapsed);
            }
            StorageManager.saveTasks(globalDatabase);
            if (refreshCallback != null) refreshCallback.run();
        }

        isFocusMode = focus;
        timeLeft = isFocusMode ? timerOptions.getValue() * 60 : 5 * 60;
        lastTrackedTimeLeft = timeLeft; // Reset sync

        statusLabel.setText(isFocusMode ? "FOCUS SESSION" : "SHORT BREAK");
        statusLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-letter-spacing: 2px; -fx-text-fill: " + (isFocusMode ? "#569CD6;" : "#4EC9B0;"));

        timeDisplay.setStyle("-fx-font-size: 85px; -fx-font-weight: bold; -fx-text-fill: " + (isFocusMode ? "#569CD6;" : "#4EC9B0;"));

        startPauseBtn.setText("START");
        startPauseBtn.setStyle("-fx-background-color: " + (isFocusMode ? "#569CD6;" : "#4EC9B0;") + " -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-font-size: 14px; -fx-background-radius: 5;");

        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = timeLeft / 60; int seconds = timeLeft % 60;
        timeDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }
}