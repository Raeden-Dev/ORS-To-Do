package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Base64;

public class GeneralSettingsPanel extends VBox {

    public GeneralSettingsPanel(AppStats appStats, Runnable refreshCallback) {
        super(15);
        setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        String sliderCss = ".slider .track { -fx-background-color: #3E3E42; -fx-background-radius: 5; } " +
                ".slider .thumb { -fx-background-color: #569CD6; } " +
                ".slider .thumb:hover { -fx-background-color: #4EC9B0; }";
        String b64 = Base64.getEncoder().encodeToString(sliderCss.getBytes());
        getStylesheets().add("data:text/css;base64," + b64);

        Label textHeader = new Label("General Configuration");
        textHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label behaviorHeader = new Label("Appearance & Behavior");
        behaviorHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");
        GridPane behaviorGrid = new GridPane();
        behaviorGrid.setHgap(15); behaviorGrid.setVgap(10);

        Spinner<Integer> fontSizeSpinner = new Spinner<>(10, 36, appStats.getTaskFontSize());
        fontSizeSpinner.setEditable(true);

        Label sliderLabel = new Label("Minimum Streak tasks to complete:");
        sliderLabel.setStyle("-fx-text-fill: white;");
        Slider streakSlider = new Slider(10, 100, appStats.getMinDailyCompletionPercent());
        streakSlider.setMajorTickUnit(10); streakSlider.setMinorTickCount(0); streakSlider.setSnapToTicks(true);
        streakSlider.setShowTickLabels(true); streakSlider.setShowTickMarks(true); streakSlider.setPrefWidth(200);

        Label sliderValueLabel = new Label((int)streakSlider.getValue() + "%");
        sliderValueLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");
        streakSlider.valueProperty().addListener((obs, oldVal, newVal) -> sliderValueLabel.setText(newVal.intValue() + "%"));
        HBox sliderBox = new HBox(10, streakSlider, sliderValueLabel);
        sliderBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox runInBackgroundCheck = new CheckBox("Run app in background (System Tray) when closed");
        runInBackgroundCheck.setSelected(appStats.isRunInBackground());
        runInBackgroundCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        CheckBox matchRectCheck = new CheckBox("Match custom prefix color to the side rectangle outline");
        matchRectCheck.setSelected(appStats.isMatchDailyRectColor());
        matchRectCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        CheckBox matchOutlineCheck = new CheckBox("Match task outline color to priority");
        matchOutlineCheck.setSelected(appStats.isMatchPriorityOutline());
        matchOutlineCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        CheckBox matchTitleColorCheck = new CheckBox("Match page title color to section sidebar color");
        matchTitleColorCheck.setSelected(appStats.isMatchTitleColor());
        matchTitleColorCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        CheckBox alwaysOnTopCheck = new CheckBox("Always keep app on top of other windows");
        alwaysOnTopCheck.setSelected(appStats.isAlwaysOnTop());
        alwaysOnTopCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // --- NEW: Zen Mode Threshold Spinner ---
        Spinner<Integer> zenSpinner = new Spinner<>(5, 100, appStats.getZenModeThreshold());
        zenSpinner.setEditable(true);

        behaviorGrid.add(new Label("Task Font Size:"), 0, 0); behaviorGrid.add(fontSizeSpinner, 1, 0);
        behaviorGrid.add(sliderLabel, 0, 1); behaviorGrid.add(sliderBox, 1, 1);
        behaviorGrid.add(runInBackgroundCheck, 0, 2, 2, 1);
        behaviorGrid.add(matchRectCheck, 0, 3, 2, 1);
        behaviorGrid.add(matchOutlineCheck, 0, 4, 2, 1);
        behaviorGrid.add(matchTitleColorCheck, 0, 5, 2, 1);
        behaviorGrid.add(alwaysOnTopCheck, 0, 6, 2, 1);
        behaviorGrid.add(new Label("Zen Mode Task Paralysis Threshold:"), 0, 7); behaviorGrid.add(zenSpinner, 1, 7);

        Label navHeader = new Label("Static Sidebar Texts & Colors");
        navHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");
        GridPane navGrid = new GridPane();
        navGrid.setHgap(15); navGrid.setVgap(10);

        TextField focusNavField = new TextField(appStats.getNavFocusText());
        TextField analyticsNavField = new TextField(appStats.getNavAnalyticsText());
        TextField archiveNavField = new TextField(appStats.getNavArchiveText());
        TextField settingsNavField = new TextField(appStats.getNavSettingsText());

        ColorPicker focusColorPicker = new ColorPicker(Color.web(appStats.getNavFocusColor()));
        ColorPicker analyticsColorPicker = new ColorPicker(Color.web(appStats.getNavAnalyticsColor()));
        ColorPicker archiveColorPicker = new ColorPicker(Color.web(appStats.getNavArchiveColor()));
        ColorPicker settingsColorPicker = new ColorPicker(Color.web(appStats.getNavSettingsColor()));

        focusColorPicker.setStyle("-fx-color-label-visible: false;");
        analyticsColorPicker.setStyle("-fx-color-label-visible: false;");
        archiveColorPicker.setStyle("-fx-color-label-visible: false;");
        settingsColorPicker.setStyle("-fx-color-label-visible: false;");

        navGrid.add(new Label("Focus Hub:"), 0, 0); navGrid.add(focusNavField, 1, 0); navGrid.add(focusColorPicker, 2, 0);
        navGrid.add(new Label("Analytics:"), 0, 1); navGrid.add(analyticsNavField, 1, 1); navGrid.add(analyticsColorPicker, 2, 1);
        navGrid.add(new Label("Archived:"), 0, 2); navGrid.add(archiveNavField, 1, 2); navGrid.add(archiveColorPicker, 2, 2);
        navGrid.add(new Label("Settings:"), 0, 3); navGrid.add(settingsNavField, 1, 3); navGrid.add(settingsColorPicker, 2, 3);

        Runnable autoSaveTrigger = () -> {
            appStats.setTaskFontSize(fontSizeSpinner.getValue());
            appStats.setMinDailyCompletionPercent((int) streakSlider.getValue());
            appStats.setRunInBackground(runInBackgroundCheck.isSelected());
            appStats.setMatchDailyRectColor(matchRectCheck.isSelected());
            appStats.setMatchPriorityOutline(matchOutlineCheck.isSelected());
            appStats.setMatchTitleColor(matchTitleColorCheck.isSelected());
            appStats.setAlwaysOnTop(alwaysOnTopCheck.isSelected());
            appStats.setZenModeThreshold(zenSpinner.getValue()); // Save Zen Threshold

            appStats.setNavFocusText(focusNavField.getText().trim().isEmpty() ? "Focus Hub" : focusNavField.getText().trim());
            appStats.setNavArchiveText(archiveNavField.getText().trim().isEmpty() ? "Archived" : archiveNavField.getText().trim());
            appStats.setNavSettingsText(settingsNavField.getText().trim().isEmpty() ? "Settings" : settingsNavField.getText().trim());
            appStats.setNavFocusColor(toHexString(focusColorPicker.getValue()));
            appStats.setNavArchiveColor(toHexString(archiveColorPicker.getValue()));
            appStats.setNavSettingsColor(toHexString(settingsColorPicker.getValue()));
            appStats.setNavAnalyticsText(analyticsNavField.getText().trim().isEmpty() ? "Analytics" : analyticsNavField.getText().trim());
            appStats.setNavAnalyticsColor(toHexString(analyticsColorPicker.getValue()));

            StorageManager.saveStats(appStats);
            refreshCallback.run();
        };

        fontSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> autoSaveTrigger.run());
        zenSpinner.valueProperty().addListener((obs, oldVal, newVal) -> autoSaveTrigger.run()); // Hook Listener

        streakSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) autoSaveTrigger.run();
        });
        streakSlider.setOnMouseReleased(e -> autoSaveTrigger.run());

        runInBackgroundCheck.setOnAction(e -> autoSaveTrigger.run());
        matchRectCheck.setOnAction(e -> autoSaveTrigger.run());
        matchOutlineCheck.setOnAction(e -> autoSaveTrigger.run());
        matchTitleColorCheck.setOnAction(e -> autoSaveTrigger.run());

        alwaysOnTopCheck.setOnAction(e -> {
            autoSaveTrigger.run();
            if (getScene() != null && getScene().getWindow() instanceof javafx.stage.Stage) {
                ((javafx.stage.Stage) getScene().getWindow()).setAlwaysOnTop(alwaysOnTopCheck.isSelected());
            }
        });

        for (TextField tf : Arrays.asList(focusNavField, analyticsNavField, archiveNavField, settingsNavField)) {
            tf.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) autoSaveTrigger.run();
            });
            tf.setOnAction(e -> autoSaveTrigger.run());
        }

        focusColorPicker.setOnAction(e -> autoSaveTrigger.run());
        analyticsColorPicker.setOnAction(e -> autoSaveTrigger.run());
        archiveColorPicker.setOnAction(e -> autoSaveTrigger.run());
        settingsColorPicker.setOnAction(e -> autoSaveTrigger.run());

        getChildren().addAll(
                textHeader, behaviorHeader, behaviorGrid, new Separator(),
                navHeader, navGrid
        );
    }

    private String toHexString(Color color) {
        if (color == null) return null;
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}