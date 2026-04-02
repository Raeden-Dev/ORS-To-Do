package com.raeden.ors_to_do.modules.dependencies;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.SystemTrayManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;

public class PomodoroTimer extends VBox {
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private Runnable refreshCallback;

    private Timeline timeline;
    private int timeLeft = 25 * 60;
    private boolean isFocusMode = true;

    private Label timeDisplay;
    private Label statusLabel;
    private Button startPauseBtn;
    private ComboBox<Integer> timerOptions;
    private ComboBox<TaskItem> taskSelector;

    public PomodoroTimer(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        super(15);
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.refreshCallback = refreshCallback;

        setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(this, Priority.ALWAYS);
        setMinWidth(450);

        statusLabel = new Label("FOCUS SESSION");
        statusLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        statusLabel.getStyleClass().add("focus-status");
        VBox.setMargin(statusLabel, new Insets(40, 0, 10, 0));

        VBox linkBox = new VBox(5);
        linkBox.setAlignment(Pos.CENTER);
        Label linkLabel = new Label("Link Focus to Task:");
        linkLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-font-weight: bold;");

        taskSelector = new ComboBox<>();
        taskSelector.setPrefWidth(300);
        taskSelector.setStyle("-fx-background-color: #3E3E42; -fx-cursor: hand;");

        taskSelector.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TaskItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("None (Free Focus)");
                } else {
                    String secName = "Task";
                    if (item.getSectionId() != null) {
                        for(AppStats.SectionConfig c : appStats.getSections()){
                            if(c.getId().equals(item.getSectionId())){ secName = c.getName(); break; }
                        }
                    }
                    setText("[" + secName + "] " + item.getTextContent());
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
                    String secName = "Task";
                    if (item.getSectionId() != null) {
                        for(AppStats.SectionConfig c : appStats.getSections()){
                            if(c.getId().equals(item.getSectionId())){ secName = c.getName(); break; }
                        }
                    }
                    setText("[" + secName + "] " + item.getTextContent());
                }
                setStyle("-fx-text-fill: white;");
            }
        });
        linkBox.getChildren().addAll(linkLabel, taskSelector);

        timeDisplay = new Label("25:00");
        timeDisplay.getStyleClass().add("timer-display");

        HBox optionsPanel = new HBox(10);
        optionsPanel.setAlignment(Pos.CENTER);
        Label focusLengthLabel = new Label("Focus Length: ");
        focusLengthLabel.setStyle("-fx-text-fill: white;");

        timerOptions = new ComboBox<>();
        timerOptions.getItems().addAll(1, 10, 25, 30, 40, 60);
        timerOptions.setValue(25);
        timerOptions.setOnAction(e -> {
            if (timeline == null || !timeline.getStatus().equals(Timeline.Status.RUNNING)) {
                if (isFocusMode) { timeLeft = timerOptions.getValue() * 60; updateDisplay(); }
            }
        });
        optionsPanel.getChildren().addAll(focusLengthLabel, timerOptions);

        HBox btnPanel = new HBox(20);
        btnPanel.setAlignment(Pos.CENTER);

        startPauseBtn = new Button("Start");
        startPauseBtn.getStyleClass().addAll("action-btn", "massive-btn");

        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().addAll("action-btn", "massive-btn");
        resetBtn.setStyle("-fx-background-color: #E06666; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        btnPanel.getChildren().addAll(startPauseBtn, resetBtn);

        getChildren().addAll(statusLabel, linkBox, timeDisplay, optionsPanel, btnPanel);
        setupTimerLogic();

        startPauseBtn.setOnAction(e -> {
            if (timeline.getStatus() == Timeline.Status.RUNNING) { timeline.pause(); startPauseBtn.setText("Resume"); }
            else { timeline.play(); startPauseBtn.setText("Pause"); }
        });

        resetBtn.setOnAction(e -> resetTimer(isFocusMode));
    }

    public void refreshTasks() {
        TaskItem selected = taskSelector.getValue();
        taskSelector.getItems().clear();
        taskSelector.getItems().add(null);

        for (TaskItem task : globalDatabase) {
            if (!task.isFinished() && !task.isArchived()) {
                boolean canFocus = false;
                if (task.getSectionId() != null) {
                    for (AppStats.SectionConfig config : appStats.getSections()) {
                        if (config.getId().equals(task.getSectionId()) && config.isTrackTime()) {
                            canFocus = true;
                            break;
                        }
                    }
                } else if (task.getOriginModule() == TaskItem.OriginModule.WORK || task.getOriginModule() == TaskItem.OriginModule.QUICK) {
                    canFocus = true;
                }
                if (canFocus) taskSelector.getItems().add(task);
            }
        }

        if (selected != null && taskSelector.getItems().contains(selected)) {
            taskSelector.setValue(selected);
        } else {
            taskSelector.setValue(null);
        }
    }

    private void setupTimerLogic() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--; updateDisplay();
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
            int totalSessionTime = timerOptions.getValue() * 60;
            if (timeLeft > 0 && timeLeft < totalSessionTime) {
                int elapsedSeconds = totalSessionTime - timeLeft;
                taskSelector.getValue().addTimeSpent(elapsedSeconds);
                StorageManager.saveTasks(globalDatabase);
                if (refreshCallback != null) refreshCallback.run();
            } else if (timeLeft <= 0) {
                taskSelector.getValue().addTimeSpent(totalSessionTime);
                StorageManager.saveTasks(globalDatabase);
                if (refreshCallback != null) refreshCallback.run();
            }
        }

        isFocusMode = focus;
        timeLeft = isFocusMode ? timerOptions.getValue() * 60 : 5 * 60;
        statusLabel.setText(isFocusMode ? "FOCUS SESSION" : "SHORT BREAK");
        statusLabel.getStyleClass().removeAll("focus-status", "break-status");
        statusLabel.getStyleClass().add(isFocusMode ? "focus-status" : "break-status");
        startPauseBtn.setText("Start");
        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = timeLeft / 60; int seconds = timeLeft % 60;
        timeDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }
}