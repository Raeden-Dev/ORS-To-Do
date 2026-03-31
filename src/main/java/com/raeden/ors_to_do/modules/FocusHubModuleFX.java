package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class FocusHubModuleFX extends HBox {
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

    private TextArea brainDumpArea;
    private WebView markdownPreview;
    private WebEngine webEngine;

    public FocusHubModuleFX(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.refreshCallback = refreshCallback;

        setSpacing(20);
        setPadding(new Insets(20));

        // --- LEFT: Pomodoro Timer ---
        VBox timerContainer = new VBox(15);
        timerContainer.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(timerContainer, Priority.ALWAYS);
        timerContainer.setMinWidth(450);

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

        // --- FIXED: Dynamic Section Name Resolution for Dropdown ---
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

        timerContainer.getChildren().addAll(statusLabel, linkBox, timeDisplay, optionsPanel, btnPanel);
        setupTimerLogic();

        startPauseBtn.setOnAction(e -> {
            if (timeline.getStatus() == Timeline.Status.RUNNING) { timeline.pause(); startPauseBtn.setText("Resume"); }
            else { timeline.play(); startPauseBtn.setText("Pause"); }
        });

        resetBtn.setOnAction(e -> resetTimer(isFocusMode));

        // --- RIGHT: Markdown Scratchpad ---
        VBox scratchpadContainer = new VBox(10);
        HBox.setHgrow(scratchpadContainer, Priority.SOMETIMES);
        scratchpadContainer.setPrefWidth(350);
        scratchpadContainer.setMaxWidth(400);

        HBox scratchHeader = new HBox();
        scratchHeader.setAlignment(Pos.CENTER_LEFT);

        Label scratchpadLabel = new Label("Scratchpad");
        scratchpadLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToggleButton previewToggle = new ToggleButton("👁 Preview");
        previewToggle.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        scratchHeader.getChildren().addAll(scratchpadLabel, spacer, previewToggle);

        StackPane editorStack = new StackPane();
        VBox.setVgrow(editorStack, Priority.ALWAYS);

        brainDumpArea = new TextArea(appStats.getBrainDumpText());
        brainDumpArea.setStyle("-fx-control-inner-background: #1E1E1E; -fx-background-color: #1E1E1E; -fx-text-fill: #E0E0E0; -fx-font-family: 'Consolas', monospace; -fx-font-size: 15px; -fx-border-color: #3E3E42;");
        brainDumpArea.setWrapText(true);
        brainDumpArea.textProperty().addListener((obs, oldText, newText) -> appStats.setBrainDumpText(newText));

        markdownPreview = new WebView();
        webEngine = markdownPreview.getEngine();
        markdownPreview.setVisible(false);

        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/atom-one-dark.min.css">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
                <style>
                    body { background-color: #1E1E1E; color: #E0E0E0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 15px; padding: 15px; margin: 0; line-height: 1.6; }
                    pre { background-color: #2D2D30; padding: 15px; border-radius: 8px; overflow-x: auto; border: 1px solid #3E3E42; }
                    code { font-family: 'Consolas', monospace; background-color: #2D2D30; padding: 3px 6px; border-radius: 4px; color: #4EC9B0; }
                    pre code { padding: 0; background-color: transparent; color: inherit; }
                    a { color: #569CD6; text-decoration: none; }
                    a:hover { text-decoration: underline; }
                    h1, h2, h3, h4 { color: #CCCCCC; border-bottom: 1px solid #3E3E42; padding-bottom: 5px; margin-top: 10px; }
                    blockquote { border-left: 4px solid #569CD6; margin: 0; padding-left: 15px; color: #AAAAAA; font-style: italic; }
                    table { border-collapse: collapse; width: 100%; margin-bottom: 15px; }
                    th, td { border: 1px solid #3E3E42; padding: 8px; text-align: left; }
                    th { background-color: #2D2D30; }
                    ::-webkit-scrollbar { width: 12px; height: 12px; }
                    ::-webkit-scrollbar-track { background: #1E1E1E; }
                    ::-webkit-scrollbar-thumb { background: #3E3E42; border-radius: 6px; border: 3px solid #1E1E1E; }
                    ::-webkit-scrollbar-thumb:hover { background: #555555; }
                </style>
            </head>
            <body>
                <div id="content"><span style="color:#AAAAAA; font-style:italic;">Initializing Markdown Engine...</span></div>
                <script>
                    function updateContent(base64Text) {
                        try {
                            const decodedText = decodeURIComponent(escape(atob(base64Text)));
                            document.getElementById('content').innerHTML = marked.parse(decodedText);
                            hljs.highlightAll();
                        } catch (e) {
                            document.getElementById('content').innerHTML = "<p style='color:#FF6666;'>Error parsing Markdown</p>";
                        }
                    }
                </script>
            </body>
            </html>
            """;

        webEngine.loadContent(htmlTemplate);

        previewToggle.setOnAction(e -> {
            if (previewToggle.isSelected()) {
                previewToggle.setText("✏️ Edit");
                brainDumpArea.setVisible(false);
                markdownPreview.setVisible(true);
                updateMarkdownPreview();
            } else {
                previewToggle.setText("👁 Preview");
                markdownPreview.setVisible(false);
                brainDumpArea.setVisible(true);
            }
        });

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED && previewToggle.isSelected()) {
                updateMarkdownPreview();
            }
        });

        editorStack.getChildren().addAll(brainDumpArea, markdownPreview);

        Button clearDumpBtn = new Button("Clear Scratchpad");
        clearDumpBtn.setMaxWidth(Double.MAX_VALUE);
        clearDumpBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        clearDumpBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Clear your notes?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> { if (response == ButtonType.YES) brainDumpArea.clear(); updateMarkdownPreview(); });
        });

        scratchpadContainer.getChildren().addAll(scratchHeader, editorStack, clearDumpBtn);
        getChildren().addAll(timerContainer, scratchpadContainer);

        refreshTasks();
    }

    private void updateMarkdownPreview() {
        String text = brainDumpArea.getText();
        if (text == null) text = "";
        String base64 = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        webEngine.executeScript("updateContent('" + base64 + "');");
    }

    // --- FIXED: Now pulls any task from any section where "Track Time" is enabled ---
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
                    canFocus = true; // Safety fallback for legacy non-migrated tasks
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

                if (isFocusMode && taskSelector.getValue() != null) {
                    int focusLengthSeconds = timerOptions.getValue() * 60;
                    taskSelector.getValue().addTimeSpent(focusLengthSeconds);
                    StorageManager.saveTasks(globalDatabase);
                    refreshCallback.run();
                }

                isFocusMode = !isFocusMode;
                resetTimer(isFocusMode);

                String title = isFocusMode ? "Pomodoro Break Over" : "Pomodoro Session Complete";
                String message = isFocusMode ? "Back to work!" : "Great job! Take a short break.";

                com.raeden.ors_to_do.TaskTrackerFXApp.pushNotification(title, message);
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