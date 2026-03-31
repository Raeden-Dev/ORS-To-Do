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
import java.util.UUID;

public class SettingsModuleFX extends ScrollPane {

    private final double BUTTON_WIDTH = 200.0;

    private VBox existingSectionsBox;
    private VBox existingTemplatesBox;
    private VBox existingPriosBox;
    private GridPane wipeGrid;

    public SettingsModuleFX(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        setBorder(Border.EMPTY);

        VBox contentBox = new VBox(30);
        contentBox.setPadding(new Insets(20));

        Label header = new Label("Control Center");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // ==========================================
        // --- 1. General Customization Section ---
        // ==========================================
        VBox textSettings = new VBox(15);
        textSettings.setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label textHeader = new Label("General Configuration");
        textHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        // --- Sub-section: Appearance & Behavior ---
        Label behaviorHeader = new Label("Appearance & Behavior");
        behaviorHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");
        GridPane behaviorGrid = new GridPane();
        behaviorGrid.setHgap(15); behaviorGrid.setVgap(10);

        Spinner<Integer> fontSizeSpinner = new Spinner<>(10, 36, appStats.getTaskFontSize());
        fontSizeSpinner.setEditable(true);

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
        streakSlider.valueProperty().addListener((obs, oldVal, newVal) -> sliderValueLabel.setText(newVal.intValue() + "%"));
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

        TextField editMenuField = new TextField(appStats.getEditMenuText());
        TextField archiveMenuField = new TextField(appStats.getArchiveMenuText());
        TextField deleteMenuField = new TextField(appStats.getDeleteMenuText());

        contextGrid.add(new Label("Edit Menu Text:"), 0, 0); contextGrid.add(editMenuField, 1, 0);
        contextGrid.add(new Label("Archive Menu Text:"), 0, 1); contextGrid.add(archiveMenuField, 1, 1);
        contextGrid.add(new Label("Delete Menu Text:"), 0, 2); contextGrid.add(deleteMenuField, 1, 2);

        // --- Sub-section: Static Navigation Config ---
        Label navHeader = new Label("Static Sidebar Texts & Colors");
        navHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");
        GridPane navGrid = new GridPane();
        navGrid.setHgap(15); navGrid.setVgap(10);

        TextField focusNavField = new TextField(appStats.getNavFocusText());
        TextField archiveNavField = new TextField(appStats.getNavArchiveText());
        TextField settingsNavField = new TextField(appStats.getNavSettingsText());

        ColorPicker focusColorPicker = new ColorPicker(Color.web(appStats.getNavFocusColor()));
        ColorPicker archiveColorPicker = new ColorPicker(Color.web(appStats.getNavArchiveColor()));
        ColorPicker settingsColorPicker = new ColorPicker(Color.web(appStats.getNavSettingsColor()));

        focusColorPicker.setStyle("-fx-color-label-visible: false;");
        archiveColorPicker.setStyle("-fx-color-label-visible: false;");
        settingsColorPicker.setStyle("-fx-color-label-visible: false;");

        navGrid.add(new Label("Focus Hub:"), 0, 0); navGrid.add(focusNavField, 1, 0); navGrid.add(focusColorPicker, 2, 0);
        navGrid.add(new Label("Archived:"), 0, 1); navGrid.add(archiveNavField, 1, 1); navGrid.add(archiveColorPicker, 2, 1);
        navGrid.add(new Label("Settings:"), 0, 2); navGrid.add(settingsNavField, 1, 2); navGrid.add(settingsColorPicker, 2, 2);

        // --- Save Button ---
        HBox saveActionBox = new HBox(15);
        saveActionBox.setAlignment(Pos.CENTER_LEFT);

        Button saveTextBtn = new Button("Save Global Changes");
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
            appStats.setMinDailyCompletionPercent((int) streakSlider.getValue());
            appStats.setRunInBackground(runInBackgroundCheck.isSelected());
            appStats.setMatchDailyRectColor(matchRectCheck.isSelected());

            appStats.setNavFocusText(focusNavField.getText().trim().isEmpty() ? "Focus Hub" : focusNavField.getText().trim());
            appStats.setNavArchiveText(archiveNavField.getText().trim().isEmpty() ? "Archived" : archiveNavField.getText().trim());
            appStats.setNavSettingsText(settingsNavField.getText().trim().isEmpty() ? "Settings" : settingsNavField.getText().trim());

            appStats.setNavFocusColor(toHexString(focusColorPicker.getValue()));
            appStats.setNavArchiveColor(toHexString(archiveColorPicker.getValue()));
            appStats.setNavSettingsColor(toHexString(settingsColorPicker.getValue()));

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
        // --- 2. THE SECTION MANAGER ---
        // ==========================================
        VBox sectionManagerBox = new VBox(15);
        sectionManagerBox.setStyle("-fx-border-color: #569CD6; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label sectionHeader = new Label("Manage Dynamic Sections");
        sectionHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #569CD6;");

        existingSectionsBox = new VBox(10);
        renderExistingSections(appStats, globalDatabase, refreshCallback);

        Button createSectionBtn = new Button("+ Create New Section");
        createSectionBtn.setPrefWidth(BUTTON_WIDTH);
        createSectionBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        createSectionBtn.setOnAction(e -> showSectionDialog(null, appStats, globalDatabase, refreshCallback));

        sectionManagerBox.getChildren().addAll(sectionHeader, existingSectionsBox, new Separator(), createSectionBtn);


        // ==========================================
        // --- 3. Template Settings Section ---
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
        // --- 4. Custom Priorities Section ---
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
        // --- 5. Danger Zone Section ---
        // ==========================================
        VBox dangerZone = new VBox(15);
        dangerZone.setStyle("-fx-border-color: #FF6666; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label dangerLabel = new Label("Danger Zone");
        dangerLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 16px; -fx-font-weight: bold;");

        wipeGrid = new GridPane();
        wipeGrid.setHgap(15); wipeGrid.setVgap(15);
        renderDangerZone(appStats, globalDatabase, refreshCallback);

        dangerZone.getChildren().addAll(dangerLabel, wipeGrid);

        contentBox.getChildren().addAll(header, sectionManagerBox, textSettings, templateSettings, prioSettings, dangerZone);
        setContent(contentBox);
    }

    private void renderExistingSections(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        existingSectionsBox.getChildren().clear();

        for (int i = 0; i < appStats.getSections().size(); i++) {
            AppStats.SectionConfig section = appStats.getSections().get(i);
            int index = i;

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);

            javafx.scene.shape.Rectangle colorRect = new javafx.scene.shape.Rectangle(12, 12);
            colorRect.setArcWidth(3); colorRect.setArcHeight(3);
            colorRect.setFill(Color.web(section.getSidebarColor()));

            Label nameLabel = new Label(section.getName());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            nameLabel.setPrefWidth(150);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox btnBox = new HBox(5);

            Button upBtn = new Button("▲");
            upBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
            upBtn.setDisable(index == 0);
            upBtn.setOnAction(e -> {
                Collections.swap(appStats.getSections(), index, index - 1);
                StorageManager.saveStats(appStats);
                renderExistingSections(appStats, globalDatabase, refreshCallback);
                refreshCallback.run();
            });

            Button downBtn = new Button("▼");
            downBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
            downBtn.setDisable(index == appStats.getSections().size() - 1);
            downBtn.setOnAction(e -> {
                Collections.swap(appStats.getSections(), index, index + 1);
                StorageManager.saveStats(appStats);
                renderExistingSections(appStats, globalDatabase, refreshCallback);
                refreshCallback.run();
            });

            Button editBtn = new Button("Edit Config");
            editBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand;");
            editBtn.setOnAction(e -> showSectionDialog(section, appStats, globalDatabase, refreshCallback));

            Button removeBtn = new Button("Delete");
            removeBtn.setStyle("-fx-background-color: #552222; -fx-text-fill: white; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure? This will permanently delete the section '" + section.getName() + "' AND ALL TASKS inside it!", ButtonType.YES, ButtonType.NO);
                alert.setHeaderText("Delete Section?");
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        appStats.getSections().remove(section);
                        globalDatabase.removeIf(t -> section.getId().equals(t.getSectionId()));
                        StorageManager.saveStats(appStats);
                        StorageManager.saveTasks(globalDatabase);

                        renderExistingSections(appStats, globalDatabase, refreshCallback);
                        renderDangerZone(appStats, globalDatabase, refreshCallback);
                        refreshCallback.run();
                    }
                });
            });

            btnBox.getChildren().addAll(upBtn, downBtn, editBtn, removeBtn);
            row.getChildren().addAll(colorRect, nameLabel, spacer, btnBox);
            existingSectionsBox.getChildren().add(row);
        }
    }

    private void showSectionDialog(AppStats.SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        boolean isNew = (config == null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Create New Section" : "Edit Section: " + config.getName());

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(15);

        TextField nameField = new TextField(isNew ? "" : config.getName());
        nameField.setPromptText("Section Name (e.g. Reading List)");
        nameField.setPrefWidth(250);

        grid.add(new Label("Section Name:"), 0, 0);
        grid.add(nameField, 1, 0, 2, 1);

        ColorPicker sideColorPicker = new ColorPicker(Color.web(isNew ? "#569CD6" : config.getSidebarColor()));
        grid.add(new Label("Sidebar Color:"), 0, 1);
        grid.add(sideColorPicker, 1, 1, 2, 1);

        CheckBox chkStreak = new CheckBox("Enable Daily Streak System");
        CheckBox chkAnalytics = new CheckBox("Show Analytics Export Button");
        CheckBox chkSubTasks = new CheckBox("Enable Sub-Tasks");
        CheckBox chkPriority = new CheckBox("Show Priority Toggles");
        CheckBox chkDate = new CheckBox("Show Creation Date");
        CheckBox chkTime = new CheckBox("Track Focus Time");
        CheckBox chkPrefix = new CheckBox("Enable Custom Prefixes");
        CheckBox chkWorkType = new CheckBox("Enable Work/Category Types");
        CheckBox chkArchive = new CheckBox("Allow Task Archiving");
        CheckBox chkTags = new CheckBox("Enable Dynamic Filter Tags");

        // --- NEW: Favorite Checkbox ---
        CheckBox chkFavorite = new CheckBox("Enable Favorite/Star System");

        if (!isNew) {
            chkStreak.setSelected(config.isHasStreak());
            chkAnalytics.setSelected(config.isShowAnalytics());
            chkSubTasks.setSelected(config.isEnableSubTasks());
            chkPriority.setSelected(config.isShowPriority());
            chkDate.setSelected(config.isShowDate());
            chkTime.setSelected(config.isTrackTime());
            chkPrefix.setSelected(config.isShowPrefix());
            chkWorkType.setSelected(config.isShowWorkType());
            chkArchive.setSelected(config.isAllowArchive());
            chkTags.setSelected(config.isShowTags());
            chkFavorite.setSelected(config.isAllowFavorite()); // Added to edit mapping
        }

        grid.add(chkStreak, 0, 2);     grid.add(chkAnalytics, 1, 2);
        grid.add(chkSubTasks, 0, 3);   grid.add(chkPriority, 1, 3);
        grid.add(chkDate, 0, 4);       grid.add(chkTime, 1, 4);
        grid.add(chkPrefix, 0, 5);     grid.add(chkWorkType, 1, 5);
        grid.add(chkArchive, 0, 6);    grid.add(chkTags, 1, 6);
        grid.add(chkFavorite, 0, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                AppStats.SectionConfig target = isNew ? new AppStats.SectionConfig(UUID.randomUUID().toString(), nameField.getText().trim()) : config;

                target.setName(nameField.getText().trim());
                target.setSidebarColor(toHexString(sideColorPicker.getValue()));

                target.setHasStreak(chkStreak.isSelected());
                target.setShowAnalytics(chkAnalytics.isSelected());
                target.setEnableSubTasks(chkSubTasks.isSelected());
                target.setShowPriority(chkPriority.isSelected());
                target.setShowDate(chkDate.isSelected());
                target.setTrackTime(chkTime.isSelected());
                target.setShowPrefix(chkPrefix.isSelected());
                target.setShowWorkType(chkWorkType.isSelected());
                target.setAllowArchive(chkArchive.isSelected());
                target.setShowTags(chkTags.isSelected());
                target.setAllowFavorite(chkFavorite.isSelected());

                if (isNew) appStats.getSections().add(target);

                StorageManager.saveStats(appStats);
                renderExistingSections(appStats, globalDatabase, refreshCallback);
                renderDangerZone(appStats, globalDatabase, refreshCallback);
                refreshCallback.run();
            }
        });
    }

    private void renderDangerZone(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        wipeGrid.getChildren().clear();
        int col = 0;
        int row = 0;

        for (AppStats.SectionConfig section : appStats.getSections()) {
            Button wipeBtn = createDangerButton("Wipe " + section.getName());
            wipeBtn.setOnAction(e -> wipeList(globalDatabase, section.getId(), refreshCallback));
            wipeGrid.add(wipeBtn, col, row);

            col++;
            if (col > 1) { col = 0; row++; }
        }

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
        wipeGrid.add(wipeAllBtn, col, row);

        col++;
        if (col > 1) { col = 0; row++; }

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
        wipeGrid.add(resetStreakBtn, col, row);
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

    private void wipeList(List<TaskItem> db, String targetSectionId, Runnable refresh) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear this list?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                db.removeIf(task -> targetSectionId.equals(task.getSectionId()));
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