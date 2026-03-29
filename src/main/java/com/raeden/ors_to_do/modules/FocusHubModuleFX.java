package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class FocusHubModuleFX extends HBox {
    private AppStats appStats;
    private Timeline timeline;
    private int timeLeft = 25 * 60;
    private boolean isFocusMode = true;

    private Label timeDisplay;
    private Label statusLabel;
    private Button startPauseBtn;
    private ComboBox<Integer> timerOptions;
    private TextArea brainDumpArea;

    public FocusHubModuleFX(AppStats appStats) {
        this.appStats = appStats;
        setSpacing(20);
        setPadding(new Insets(20));

        // --- LEFT: Pomodoro Timer ---
        VBox timerContainer = new VBox(15);
        timerContainer.setAlignment(Pos.TOP_CENTER); // Moved to top
        HBox.setHgrow(timerContainer, Priority.ALWAYS);

        statusLabel = new Label("FOCUS SESSION");
        statusLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;"); // Made larger
        statusLabel.getStyleClass().add("focus-status");
        VBox.setMargin(statusLabel, new Insets(40, 0, 20, 0)); // Push down slightly

        timeDisplay = new Label("25:00");
        timeDisplay.getStyleClass().add("timer-display");

        HBox optionsPanel = new HBox(10);
        optionsPanel.setAlignment(Pos.CENTER);
        Label focusLengthLabel = new Label("Focus Length: ");
        focusLengthLabel.setStyle("-fx-text-fill: white;");

        timerOptions = new ComboBox<>();
        timerOptions.getItems().addAll(10, 25, 30, 40, 60);
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

        btnPanel.getChildren().addAll(startPauseBtn, resetBtn);

        timerContainer.getChildren().addAll(statusLabel, timeDisplay, optionsPanel, btnPanel);
        setupTimerLogic();

        startPauseBtn.setOnAction(e -> {
            if (timeline.getStatus() == Timeline.Status.RUNNING) { timeline.pause(); startPauseBtn.setText("Resume"); }
            else { timeline.play(); startPauseBtn.setText("Pause"); }
        });

        resetBtn.setOnAction(e -> resetTimer(isFocusMode));

        // --- RIGHT: Scratchpad ---
        VBox scratchpadContainer = new VBox(10);
        HBox.setHgrow(scratchpadContainer, Priority.ALWAYS);

        Label scratchpadLabel = new Label("Scratchpad"); // Renamed
        scratchpadLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");

        brainDumpArea = new TextArea(appStats.getBrainDumpText());
        brainDumpArea.getStyleClass().add("scratchpad"); // Dark gray applied via CSS
        brainDumpArea.setWrapText(true);
        VBox.setVgrow(brainDumpArea, Priority.ALWAYS);

        brainDumpArea.textProperty().addListener((obs, oldText, newText) -> appStats.setBrainDumpText(newText));

        Button clearDumpBtn = new Button("Clear Scratchpad");
        clearDumpBtn.setMaxWidth(Double.MAX_VALUE);
        clearDumpBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Clear your notes?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> { if (response == ButtonType.YES) brainDumpArea.clear(); });
        });

        scratchpadContainer.getChildren().addAll(scratchpadLabel, brainDumpArea, clearDumpBtn);
        getChildren().addAll(timerContainer, scratchpadContainer);
    }

    private void setupTimerLogic() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--; updateDisplay();
            if (timeLeft <= 0) {
                timeline.pause(); isFocusMode = !isFocusMode; resetTimer(isFocusMode);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Pomodoro"); alert.setHeaderText(null);
                alert.setContentText(isFocusMode ? "Break over! Back to work." : "Focus session complete. Take a break!");
                alert.show();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void resetTimer(boolean focus) {
        if (timeline != null) timeline.pause();
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