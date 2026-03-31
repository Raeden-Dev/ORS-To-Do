package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Collections;
import java.util.List;

public class SettingsModuleFX extends ScrollPane {

    private final double BUTTON_WIDTH = 200.0;
    private VBox existingPriosBox;
    private VBox existingTemplatesBox;

    public SettingsModuleFX(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        setBorder(Border.EMPTY);

        VBox contentBox = new VBox(30);
        contentBox.setPadding(new Insets(20));

        Label header = new Label("Settings");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // ==========================================
        // --- 1. General Customization Section ---
        // ==========================================
        VBox textSettings = new VBox(15);
        textSettings.setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label textHeader = new Label("General Customization");
        textHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        // --- Sub-section: Appearance & Behavior ---
        Label behaviorHeader = new Label("Appearance & Behavior");
        behaviorHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");
        GridPane behaviorGrid = new GridPane();
        behaviorGrid.setHgap(15); behaviorGrid.setVgap(10);

        Spinner<Integer> fontSizeSpinner = new Spinner<>(10, 36, appStats.getTaskFontSize());
        fontSizeSpinner.setEditable(true);

        // --- NEW: Leniency Slider UI ---
        Label sliderLabel = new Label("Minimum Daily tasks to complete:");
        sliderLabel.setStyle("-fx-text-fill: white;");
        Slider streakSlider = new Slider(10, 100, appStats.getMinDailyCompletionPercent());
        streakSlider.setMajorTickUnit(10);
        streakSlider.setMinorTickCount(0);
        streakSlider.setSnapToTicks(true);
        streakSlider.setShowTickLabels(true);
        streakSlider.setShowTickMarks(true);
        streakSlider.setPrefWidth(200);

        Label sliderValueLabel = new Label((int)streakSlider.getValue() + "%");
        sliderValueLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");
        streakSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sliderValueLabel.setText(newVal.intValue() + "%");
        });
        HBox sliderBox = new HBox(10, streakSlider, sliderValueLabel);
        sliderBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox runInBackgroundCheck = new CheckBox("Run app in background (System Tray) when closed");
        runInBackgroundCheck.setSelected(appStats.isRunInBackground());
        runInBackgroundCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        CheckBox matchRectCheck = new CheckBox("Match Daily prefix color to the white side rectangle");
        matchRectCheck.setSelected(appStats.isMatchDailyRectColor());
        matchRectCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        behaviorGrid.add(new Label("Task Font Size:"), 0, 0); behaviorGrid.add(fontSizeSpinner, 1, 0);
        behaviorGrid.add(sliderLabel, 0, 1); behaviorGrid.add(sliderBox, 1, 1);
        behaviorGrid.add(runInBackgroundCheck, 0, 2, 2, 1);
        behaviorGrid.add(matchRectCheck, 0, 3, 2, 1);

        // --- Sub-section: Context Menu Texts ---
        Label contextHeader = new Label("Right-Click Menu Texts");
        contextHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");
        GridPane contextGrid = new GridPane();
        contextGrid.setHgap(15); contextGrid.setVgap(10);

        TextField editMenuField = new TextField(appStats.getEditMenuText()); editMenuField.setPromptText("Default: Edit Task");
        TextField archiveMenuField = new TextField(appStats.getArchiveMenuText()); archiveMenuField.setPromptText("Default: Archive Task");
        TextField deleteMenuField = new TextField(appStats.getDeleteMenuText()); deleteMenuField.setPromptText("Default: Delete");

        contextGrid.add(new Label("Edit Menu Text:"), 0, 0); contextGrid.add(editMenuField, 1, 0);
        contextGrid.add(new Label("Archive Menu Text:"), 0, 1); contextGrid.add(archiveMenuField, 1, 1);
        contextGrid.add(new Label("Delete Menu Text:"), 0, 2); contextGrid.add(deleteMenuField, 1, 2);

        // --- Sub-section: Navigation Texts ---
        Label navHeader = new Label("Sidebar Navigation Texts");
        navHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");
        GridPane navGrid = new GridPane();
        navGrid.setHgap(15); navGrid.setVgap(10);

        TextField quickNavField = new TextField(appStats.getNavQuickText()); quickNavField.setPromptText("Default: Quick To-Do");
        TextField dailyNavField = new TextField(appStats.getNavDailyText()); dailyNavField.setPromptText("Default: Daily To-Do");
        TextField workNavField = new TextField(appStats.getNavWorkText()); workNavField.setPromptText("Default: Work List");
        TextField focusNavField = new TextField(appStats.getNavFocusText()); focusNavField.setPromptText("Default: Focus Hub");
        TextField archiveNavField = new TextField(appStats.getNavArchiveText()); archiveNavField.setPromptText("Default: Archived");
        TextField settingsNavField = new TextField(appStats.getNavSettingsText()); settingsNavField.setPromptText("Default: Settings");

        navGrid.add(new Label("Quick To-Do Nav:"), 0, 0); navGrid.add(quickNavField, 1, 0);
        navGrid.add(new Label("Daily To-Do Nav:"), 0, 1); navGrid.add(dailyNavField, 1, 1);
        navGrid.add(new Label("Work List Nav:"), 0, 2); navGrid.add(workNavField, 1, 2);
        navGrid.add(new Label("Focus Hub Nav:"), 2, 0); navGrid.add(focusNavField, 3, 0);
        navGrid.add(new Label("Archived Nav:"), 2, 1); navGrid.add(archiveNavField, 3, 1);
        navGrid.add(new Label("Settings Nav:"), 2, 2); navGrid.add(settingsNavField, 3, 2);

        // --- Save Button & 3-Second Notification ---
        HBox saveActionBox = new HBox(15);
        saveActionBox.setAlignment(Pos.CENTER_LEFT);

        Button saveTextBtn = new Button("Save Changes");
        saveTextBtn.setPrefWidth(BUTTON_WIDTH);
        saveTextBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Label savedNotification = new Label("Saved Changes!");
        savedNotification.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold; -fx-font-size: 14px;");
        savedNotification.setVisible(false);

        saveTextBtn.setOnAction(e -> {
            appStats.setEditMenuText(editMenuField.getText().trim().isEmpty() ? "Edit Task" : editMenuField.getText().trim());
            appStats.setArchiveMenuText(archiveMenuField.getText().trim().isEmpty() ? "Archive Task" : archiveMenuField.getText().trim());
            appStats.setDeleteMenuText(deleteMenuField.getText().trim().isEmpty() ? "Delete" : deleteMenuField.getText().trim());
            appStats.setTaskFontSize(fontSizeSpinner.getValue());
            appStats.setMinDailyCompletionPercent((int) streakSlider.getValue()); // Saves to Data
            appStats.setRunInBackground(runInBackgroundCheck.isSelected());
            appStats.setMatchDailyRectColor(matchRectCheck.isSelected());

            appStats.setNavQuickText(quickNavField.getText().trim().isEmpty() ? "Quick To-Do" : quickNavField.getText().trim());
            appStats.setNavDailyText(dailyNavField.getText().trim().isEmpty() ? "Daily To-Do" : dailyNavField.getText().trim());
            appStats.setNavWorkText(workNavField.getText().trim().isEmpty() ? "Work List" : workNavField.getText().trim());
            appStats.setNavFocusText(focusNavField.getText().trim().isEmpty() ? "Focus Hub" : focusNavField.getText().trim());
            appStats.setNavArchiveText(archiveNavField.getText().trim().isEmpty() ? "Archived" : archiveNavField.getText().trim());
            appStats.setNavSettingsText(settingsNavField.getText().trim().isEmpty() ? "Settings" : settingsNavField.getText().trim());

            StorageManager.saveStats(appStats);
            refreshCallback.run();

            savedNotification.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> savedNotification.setVisible(false));
            pause.play();
        });

        saveActionBox.getChildren().addAll(saveTextBtn, savedNotification);

        textSettings.getChildren().addAll(
                textHeader,
                behaviorHeader, behaviorGrid, new Separator(),
                contextHeader, contextGrid, new Separator(),
                navHeader, navGrid, new Separator(),
                saveActionBox
        );

        // ==========================================
        // --- 2. Template Settings Section ---
        // ==========================================
        VBox templateSettings = new VBox(15);
        templateSettings.setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label templateHeader = new Label("Daily Tasks to Add");
        templateHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #CCCCCC;");

        existingTemplatesBox = new VBox(10);
        renderExistingTemplates(appStats, refreshCallback);

        HBox templateInput = new HBox(10);
        templateInput.setAlignment(Pos.CENTER_LEFT);
        TextField tempPrefix = new TextField(); tempPrefix.setPromptText("[Prefix]"); tempPrefix.setPrefWidth(80);
        ColorPicker prefixColorPicker = new ColorPicker(Color.web("#4EC9B0"));
        prefixColorPicker.setStyle("-fx-color-label-visible: false;");

        TextField tempText = new TextField(); tempText.setPromptText("Task Content");
        HBox.setHgrow(tempText, Priority.ALWAYS);
        ColorPicker bgColorPicker = new ColorPicker(Color.TRANSPARENT);
        bgColorPicker.setStyle("-fx-color-label-visible: false;");

        Button addTempBtn = new Button("Add Task");
        addTempBtn.setPrefWidth(120);
        addTempBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-border-color: #555555; -fx-cursor: hand;");
        addTempBtn.setOnAction(e -> {
            if(!tempText.getText().isEmpty()) {
                String cleanPrefix = tempPrefix.getText().trim().toUpperCase();
                if (!cleanPrefix.isEmpty()) {
                    if (!cleanPrefix.startsWith("[")) cleanPrefix = "[" + cleanPrefix;
                    if (!cleanPrefix.endsWith("]")) cleanPrefix = cleanPrefix + "]";
                }

                String pColor = toHexString(prefixColorPicker.getValue());
                String bColor = bgColorPicker.getValue().getOpacity() == 0.0 ? null : toHexString(bgColorPicker.getValue());

                appStats.getBaseDailies().add(new AppStats.DailyTemplate(cleanPrefix, tempText.getText().trim(), pColor, bColor));
                StorageManager.saveStats(appStats);
                renderExistingTemplates(appStats, refreshCallback);
                tempPrefix.clear(); tempText.clear();
            }
        });

        templateInput.getChildren().addAll(tempPrefix, prefixColorPicker, tempText, new Label("Bg:"), bgColorPicker, addTempBtn);
        templateSettings.getChildren().addAll(templateHeader, existingTemplatesBox, new Separator(), templateInput);


        // ==========================================
        // --- 3. Custom Priorities Section ---
        // ==========================================
        VBox prioSettings = new VBox(15);
        prioSettings.setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label prioHeader = new Label("Manage Priorities");
        prioHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #CCCCCC;");

        existingPriosBox = new VBox(10);
        renderExistingPriorities(appStats, refreshCallback);

        HBox prioInput = new HBox(10);
        TextField prioName = new TextField(); prioName.setPromptText("Priority Name (e.g. URGENT)");
        ColorPicker colorPicker = new ColorPicker();

        Button addPrioBtn = new Button("Add Priority");
        addPrioBtn.setPrefWidth(BUTTON_WIDTH);
        addPrioBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-border-color: #555555; -fx-cursor: hand;");
        addPrioBtn.setOnAction(e -> {
            if(!prioName.getText().isEmpty()) {
                appStats.getCustomPriorities().add(new TaskItem.CustomPriority(prioName.getText(), toHexString(colorPicker.getValue())));
                StorageManager.saveStats(appStats);
                renderExistingPriorities(appStats, refreshCallback);
                refreshCallback.run();
                prioName.clear();
            }
        });
        prioInput.getChildren().addAll(prioName, colorPicker);
        prioSettings.getChildren().addAll(prioHeader, existingPriosBox, new Separator(), prioInput, addPrioBtn);


        // ==========================================
        // --- 4. Danger Zone Section ---
        // ==========================================
        VBox dangerZone = new VBox(15);
        dangerZone.setStyle("-fx-border-color: #FF6666; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label dangerLabel = new Label("Danger Zone");
        dangerLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane wipeGrid = new GridPane();
        wipeGrid.setHgap(15); wipeGrid.setVgap(15);

        Button wipeQuickBtn = createDangerButton("Wipe Quick To-Do");
        wipeQuickBtn.setOnAction(e -> wipeList(globalDatabase, TaskItem.OriginModule.QUICK, refreshCallback));

        Button wipeDailyBtn = createDangerButton("Wipe Daily To-Do");
        wipeDailyBtn.setOnAction(e -> wipeList(globalDatabase, TaskItem.OriginModule.DAILY, refreshCallback));

        Button wipeWorkBtn = createDangerButton("Wipe Work List");
        wipeWorkBtn.setOnAction(e -> wipeList(globalDatabase, TaskItem.OriginModule.WORK, refreshCallback));

        Button wipeAllBtn = new Button("Wipe ALL Tasks");
        wipeAllBtn.setPrefWidth(BUTTON_WIDTH);
        wipeAllBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        wipeAllBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently delete ALL tasks?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    globalDatabase.clear();
                    StorageManager.saveTasks(globalDatabase);
                    refreshCallback.run();
                }
            });
        });

        Button resetStreakBtn = createDangerButton("Reset Daily Streak");
        resetStreakBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset your daily streak to 0?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    appStats.setCurrentStreak(0);
                    StorageManager.saveStats(appStats);
                    refreshCallback.run();
                }
            });
        });

        wipeGrid.add(wipeQuickBtn, 0, 0);
        wipeGrid.add(wipeDailyBtn, 1, 0);
        wipeGrid.add(wipeWorkBtn, 0, 1);
        wipeGrid.add(wipeAllBtn, 1, 1);
        wipeGrid.add(resetStreakBtn, 0, 2);

        dangerZone.getChildren().addAll(dangerLabel, wipeGrid);

        contentBox.getChildren().addAll(header, textSettings, templateSettings, prioSettings, dangerZone);
        setContent(contentBox);
    }

    private void renderExistingTemplates(AppStats appStats, Runnable refreshCallback) {
        existingTemplatesBox.getChildren().clear();

        for (int i = 0; i < appStats.getBaseDailies().size(); i++) {
            AppStats.DailyTemplate temp = appStats.getBaseDailies().get(i);
            int index = i;

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            if (temp.getBgColor() != null) row.setStyle("-fx-background-color: " + temp.getBgColor() + "; -fx-background-radius: 3;");

            Label prefixLabel = new Label(temp.getPrefix() != null ? temp.getPrefix() : "");
            prefixLabel.setStyle("-fx-text-fill: " + temp.getPrefixColor() + "; -fx-font-weight: bold; -fx-font-size: 14px;");
            prefixLabel.setPrefWidth(80);

            Label textLabel = new Label(temp.getText());
            textLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox btnBox = new HBox(5);

            Button upBtn = new Button("▲");
            upBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
            upBtn.setDisable(index == 0);
            upBtn.setOnAction(e -> {
                Collections.swap(appStats.getBaseDailies(), index, index - 1);
                StorageManager.saveStats(appStats);
                renderExistingTemplates(appStats, refreshCallback);
            });

            Button downBtn = new Button("▼");
            downBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
            downBtn.setDisable(index == appStats.getBaseDailies().size() - 1);
            downBtn.setOnAction(e -> {
                Collections.swap(appStats.getBaseDailies(), index, index + 1);
                StorageManager.saveStats(appStats);
                renderExistingTemplates(appStats, refreshCallback);
            });

            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand;");
            editBtn.setOnAction(e -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit Template");
                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10);

                TextField preF = new TextField(temp.getPrefix());
                ColorPicker preC = new ColorPicker(Color.web(temp.getPrefixColor()));
                TextField txtF = new TextField(temp.getText());
                ColorPicker bgC = new ColorPicker(temp.getBgColor() != null ? Color.web(temp.getBgColor()) : Color.TRANSPARENT);

                Button clearBgBtn = new Button("Clear");
                clearBgBtn.setOnAction(ev -> bgC.setValue(Color.TRANSPARENT));
                HBox bgBox = new HBox(5, bgC, clearBgBtn);

                grid.add(new Label("Prefix:"), 0, 0); grid.add(preF, 1, 0);
                grid.add(new Label("Prefix Color:"), 0, 1); grid.add(preC, 1, 1);
                grid.add(new Label("Content:"), 0, 2); grid.add(txtF, 1, 2);
                grid.add(new Label("BG Color:"), 0, 3); grid.add(bgBox, 1, 3);

                dialog.getDialogPane().setContent(grid);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                dialog.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        temp.setPrefix(preF.getText().trim());
                        temp.setText(txtF.getText().trim());
                        temp.setPrefixColor(toHexString(preC.getValue()));
                        temp.setBgColor(bgC.getValue().getOpacity() == 0.0 ? null : toHexString(bgC.getValue()));
                        StorageManager.saveStats(appStats);
                        renderExistingTemplates(appStats, refreshCallback);
                    }
                });
            });

            Button removeBtn = new Button("Remove");
            removeBtn.setStyle("-fx-background-color: #552222; -fx-text-fill: white; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                appStats.getBaseDailies().remove(temp);
                StorageManager.saveStats(appStats);
                renderExistingTemplates(appStats, refreshCallback);
            });

            btnBox.getChildren().addAll(upBtn, downBtn, editBtn, removeBtn);
            row.getChildren().addAll(prefixLabel, textLabel, spacer, btnBox);
            existingTemplatesBox.getChildren().add(row);
        }
    }

    private void renderExistingPriorities(AppStats appStats, Runnable refreshCallback) {
        existingPriosBox.getChildren().clear();

        for (TaskItem.CustomPriority prio : appStats.getCustomPriorities()) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(prio.getName());
            nameLabel.setStyle("-fx-text-fill: " + prio.getColorHex() + "; -fx-font-weight: bold; -fx-font-size: 14px;");
            nameLabel.setPrefWidth(150);

            Button removeBtn = new Button("Remove");
            removeBtn.setStyle("-fx-background-color: #552222; -fx-text-fill: white; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                if (appStats.getCustomPriorities().size() <= 1) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "You must have at least one priority left in the system.");
                    alert.setHeaderText(null);
                    alert.show();
                    return;
                }
                appStats.getCustomPriorities().remove(prio);
                StorageManager.saveStats(appStats);
                renderExistingPriorities(appStats, refreshCallback);
                refreshCallback.run();
            });

            row.getChildren().addAll(nameLabel, removeBtn);
            existingPriosBox.getChildren().add(row);
        }
    }

    private Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(BUTTON_WIDTH);
        btn.setStyle("-fx-background-color: #333333; -fx-text-fill: #FF6666; -fx-border-color: #FF6666; -fx-border-radius: 3; -fx-cursor: hand;");
        return btn;
    }

    private void wipeList(List<TaskItem> db, TaskItem.OriginModule module, Runnable refresh) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear this list?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                db.removeIf(task -> task.getOriginModule() == module);
                StorageManager.saveTasks(db);
                refresh.run();
            }
        });
    }

    private String toHexString(Color color) {
        if (color == null) return null;
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}