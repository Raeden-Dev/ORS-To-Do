package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SectionManagerPanel extends VBox {

    private VBox existingSectionsBox;
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private Runnable refreshCallback;
    private Runnable onSectionChanged;

    public SectionManagerPanel(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback, Runnable onSectionChanged) {
        super(15);
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.refreshCallback = refreshCallback;
        this.onSectionChanged = onSectionChanged;

        setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label textHeader = new Label("Manage Dynamic Sections");
        textHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button addSectionBtn = new Button("+ Add Section");
        addSectionBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15;");
        addSectionBtn.setPrefWidth(250);

        topRow.getChildren().add(addSectionBtn);

        existingSectionsBox = new VBox(10);

        addSectionBtn.setOnAction(e -> {
            SectionConfig newConfig = new SectionConfig(UUID.randomUUID().toString(), "");
            showEditSectionDialog(newConfig, true);
        });

        getChildren().addAll(textHeader, topRow, new Separator(), existingSectionsBox);
        refreshList();
    }

    private void refreshList() {
        existingSectionsBox.getChildren().clear();

        for (SectionConfig config : appStats.getSections()) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Rectangle colorIndicator = new Rectangle(12, 12, Color.web(config.getSidebarColor()));
            colorIndicator.setStroke(Color.web("#555555"));

            Label nameLabel = new Label(config.getName());
            nameLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px; -fx-font-weight: bold;");

            HBox nameBox = new HBox(10, colorIndicator, nameLabel);
            nameBox.setAlignment(Pos.CENTER_LEFT);

            // --- FIXED: Visual Badge so the interval is visible directly on the Dashboard ---
            if (config.getResetIntervalHours() > 0) {
                Label intervalBadge = new Label("⏱ " + config.getResetIntervalHours() + "h Reset");
                intervalBadge.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px; -fx-background-color: #2D2D30; -fx-padding: 3 8; -fx-background-radius: 10; -fx-border-color: #3E3E42; -fx-border-radius: 10;");
                nameBox.getChildren().add(intervalBadge);
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button editBtn = new Button("⚙ Edit Features");
            editBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
            editBtn.setOnAction(e -> showEditSectionDialog(config, false));

            Button moveUpBtn = new Button("▲");
            moveUpBtn.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-border-color: #3E3E42; -fx-border-radius: 3;");
            moveUpBtn.setOnAction(e -> moveSection(config, -1));

            Button moveDownBtn = new Button("▼");
            moveDownBtn.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-border-color: #3E3E42; -fx-border-radius: 3;");
            moveDownBtn.setOnAction(e -> moveSection(config, 1));

            Button deleteBtn = new Button("❌");
            deleteBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete section '" + config.getName() + "'?\n\nThis will permanently delete all tasks inside it!", ButtonType.YES, ButtonType.NO);
                alert.setHeaderText("Delete Section");
                TaskDialogs.styleDialog(alert);
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        appStats.getSections().remove(config);

                        List<TaskItem> toRemove = new java.util.ArrayList<>();
                        for (TaskItem task : globalDatabase) {
                            if (config.getId().equals(task.getSectionId())) {
                                toRemove.add(task);
                            }
                        }
                        globalDatabase.removeAll(toRemove);

                        StorageManager.saveStats(appStats);
                        StorageManager.saveTasks(globalDatabase);
                        refreshList();
                        if(onSectionChanged != null) onSectionChanged.run();
                        refreshCallback.run();
                    }
                });
            });

            row.getChildren().addAll(nameBox, spacer, moveUpBtn, moveDownBtn, editBtn, deleteBtn);
            existingSectionsBox.getChildren().add(row);
        }
    }

    private void moveSection(SectionConfig config, int offset) {
        int currentIndex = appStats.getSections().indexOf(config);
        int newIndex = currentIndex + offset;

        if (newIndex >= 0 && newIndex < appStats.getSections().size()) {
            Collections.swap(appStats.getSections(), currentIndex, newIndex);
            StorageManager.saveStats(appStats);
            refreshList();
            if(onSectionChanged != null) onSectionChanged.run();
            refreshCallback.run();
        }
    }

    private VBox createToggleWithDesc(CheckBox cb, String desc) {
        cb.setStyle("-fx-text-fill: white;");
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px;");
        VBox box = new VBox(2, cb, descLabel);
        return box;
    }

    private void showEditSectionDialog(SectionConfig config, boolean isNew) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Create New Section" : "Edit Section: " + config.getName());
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        content.setPrefWidth(700);

        HBox presetRow = new HBox(10);
        presetRow.setAlignment(Pos.CENTER_LEFT);
        presetRow.setStyle("-fx-background-color: #2D2D30; -fx-padding: 10; -fx-border-color: #3E3E42; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label presetLabel = new Label("Load Preset:");
        presetLabel.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold;");

        ComboBox<SectionConfig> presetBox = new ComboBox<>();
        presetBox.setStyle("-fx-background-color: #3E3E42; -fx-cursor: hand;");
        presetBox.setPrefWidth(200);
        presetBox.getItems().add(null);
        presetBox.getItems().addAll(appStats.getSectionPresets());

        presetBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(SectionConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Preset..." : item.getName());
                setStyle("-fx-text-fill: " + (empty || item == null ? "#AAAAAA" : "black") + ";");
            }
        });
        presetBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(SectionConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Preset..." : item.getName());
                setStyle("-fx-text-fill: white;");
            }
        });

        Button savePresetBtn = new Button("💾 Save Current Config as Preset");
        savePresetBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");

        Region presetSpacer = new Region();
        HBox.setHgrow(presetSpacer, Priority.ALWAYS);

        presetRow.getChildren().addAll(presetLabel, presetBox, presetSpacer, savePresetBtn);
        content.getChildren().add(presetRow);

        HBox basicInfoRow = new HBox(15);
        basicInfoRow.setAlignment(Pos.CENTER_LEFT);

        VBox nameBox = new VBox(5, new Label("Name:"), new TextField(config.getName()));
        ((TextField)nameBox.getChildren().get(1)).setPrefWidth(300);

        ColorPicker colorPicker = new ColorPicker(Color.web(config.getSidebarColor()));
        colorPicker.setStyle("-fx-color-label-visible: false;");
        VBox colorBox = new VBox(5, new Label("Theme Color:"), colorPicker);

        Spinner<Integer> intervalSpinner = new Spinner<>(0, 8760, config.getResetIntervalHours());
        intervalSpinner.setEditable(true);
        intervalSpinner.setPrefWidth(80);

        // --- FIXED: Intercept keystrokes instantly so the Editable Spinner bug never occurs ---
        intervalSpinner.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            try {
                int val = Integer.parseInt(newText.trim());
                intervalSpinner.getValueFactory().setValue(val);
            } catch (NumberFormatException ignored) {}
        });

        // Backup safeguard if they empty the text box entirely and click away
        intervalSpinner.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                try {
                    int val = Integer.parseInt(intervalSpinner.getEditor().getText().trim());
                    intervalSpinner.getValueFactory().setValue(val);
                } catch (NumberFormatException e) {
                    intervalSpinner.getEditor().setText(String.valueOf(intervalSpinner.getValue()));
                }
            }
        });

        VBox intervalBox = new VBox(5, new Label("Reset Interval (Hours):"), intervalSpinner);

        basicInfoRow.getChildren().addAll(nameBox, colorBox, intervalBox);
        content.getChildren().addAll(basicInfoRow, new Separator());

        GridPane featuresGrid = new GridPane();
        featuresGrid.setHgap(20);
        featuresGrid.setVgap(15);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        featuresGrid.getColumnConstraints().addAll(col1, col2);

        CheckBox allowManualArchiveCheck = new CheckBox("Allow Manual Archiving");
        allowManualArchiveCheck.setSelected(config.isAllowManualArchiving());

        CheckBox enableSubTasksCheck = new CheckBox("Enable Sub-Tasks");
        enableSubTasksCheck.setSelected(config.isEnableSubTasks());

        CheckBox showDateCheck = new CheckBox("Show Creation Date");
        showDateCheck.setSelected(config.isShowDate());

        CheckBox showPrefixCheck = new CheckBox("Enable Custom Prefixes");
        showPrefixCheck.setSelected(config.isShowPrefix());

        CheckBox showTagsCheck = new CheckBox("Enable Dynamic Filter Tags");
        showTagsCheck.setSelected(config.isShowTags());

        CheckBox enableScoreCheck = new CheckBox("Enable Point System (Reward/Penalty)");
        enableScoreCheck.setSelected(config.isEnableScore());

        CheckBox enableLinksCheck = new CheckBox("Enable Task Links");
        enableLinksCheck.setSelected(config.isEnableLinks());

        CheckBox rewardsPageCheck = new CheckBox("Enable Rewards Shop Mode");
        rewardsPageCheck.setSelected(config.isRewardsPage());
        rewardsPageCheck.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold;");

        featuresGrid.add(createToggleWithDesc(allowManualArchiveCheck, "Enables right-click to send tasks to Archive."), 0, 0);
        featuresGrid.add(createToggleWithDesc(enableSubTasksCheck, "Allows creating nested to-do items inside a card."), 0, 1);
        featuresGrid.add(createToggleWithDesc(showDateCheck, "Displays the exact date the task was generated."), 0, 2);
        featuresGrid.add(createToggleWithDesc(showPrefixCheck, "Allows prefixing tags like [GYM] with custom colors."), 0, 3);
        featuresGrid.add(createToggleWithDesc(showTagsCheck, "Auto-generates clickable sorting buttons at the top of the page."), 0, 4);
        featuresGrid.add(createToggleWithDesc(enableScoreCheck, "Allows adding and earning score points for tasks."), 0, 5);
        featuresGrid.add(createToggleWithDesc(enableLinksCheck, "Allows attaching clickable URLs to tasks."), 0, 6);
        featuresGrid.add(createToggleWithDesc(rewardsPageCheck, "Turns this list into a shop where items cost points instead of giving them."), 0, 7);

        CheckBox streakCheck = new CheckBox("Enable Streak System");
        streakCheck.setSelected(config.isHasStreak());
        streakCheck.setDisable(config.getResetIntervalHours() <= 0);

        CheckBox autoArchiveCheck = new CheckBox("Auto-Archive Completed");
        autoArchiveCheck.setSelected(config.isAutoArchive());

        CheckBox showPriorityCheck = new CheckBox("Show Priority Toggles");
        showPriorityCheck.setSelected(config.isShowPriority());

        CheckBox trackTimeCheck = new CheckBox("Track Focus Time");
        trackTimeCheck.setSelected(config.isTrackTime());

        CheckBox showTaskTypeCheck = new CheckBox("Enable Work Types");
        showTaskTypeCheck.setSelected(config.isShowTaskType());

        CheckBox favoriteCheck = new CheckBox("Enable Favorite System");
        favoriteCheck.setSelected(config.isAllowFavorite());

        CheckBox showAnalyticsCheck = new CheckBox("Show Analytics Export");
        showAnalyticsCheck.setSelected(config.isShowAnalytics());

        CheckBox enableIconsCheck = new CheckBox("Enable Task Icons");
        enableIconsCheck.setSelected(config.isEnableIcons());

        CheckBox enableZenModeCheck = new CheckBox("Enable Zen Mode");
        enableZenModeCheck.setSelected(config.isEnableZenMode());

        CheckBox enableOptionalTasksCheck = new CheckBox("Enable Optional Tasks");
        enableOptionalTasksCheck.setSelected(config.isEnableOptionalTasks());
        enableOptionalTasksCheck.setDisable(config.getResetIntervalHours() <= 0 || !config.isEnableScore());

        CheckBox enableTaskStylingCheck = new CheckBox("Enable Task Styling");
        enableTaskStylingCheck.setSelected(config.isEnableTaskStyling());

        // Because we added the textProperty listener above, this logic now correctly fires instantly when typing!
        intervalSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasInterval = newVal != null && newVal > 0;
            streakCheck.setDisable(!hasInterval);
            if (!hasInterval) streakCheck.setSelected(false);

            enableOptionalTasksCheck.setDisable(!hasInterval || !enableScoreCheck.isSelected());
            if (!hasInterval || !enableScoreCheck.isSelected()) enableOptionalTasksCheck.setSelected(false);
        });

        enableScoreCheck.setOnAction(e -> {
            boolean hasInterval = intervalSpinner.getValue() != null && intervalSpinner.getValue() > 0;
            enableOptionalTasksCheck.setDisable(!hasInterval || !enableScoreCheck.isSelected());
            if (!hasInterval || !enableScoreCheck.isSelected()) enableOptionalTasksCheck.setSelected(false);
        });

        featuresGrid.add(createToggleWithDesc(streakCheck, "Tracks consecutive completions. Requires a reset interval."), 1, 0);
        featuresGrid.add(createToggleWithDesc(autoArchiveCheck, "Tasks are sent to archive the moment they are checked off."), 1, 1);
        featuresGrid.add(createToggleWithDesc(showPriorityCheck, "Adds a priority ranking dropdown to each task."), 1, 2);
        featuresGrid.add(createToggleWithDesc(trackTimeCheck, "Links tasks to the Pomodoro Focus Hub timer."), 1, 3);
        featuresGrid.add(createToggleWithDesc(showTaskTypeCheck, "Displays an editable string box for categorization."), 1, 4);
        featuresGrid.add(createToggleWithDesc(favoriteCheck, "Allows starring tasks for a golden border override."), 1, 5);
        featuresGrid.add(createToggleWithDesc(showAnalyticsCheck, "Displays a button to export an HTML graph of completed tasks."), 1, 6);
        featuresGrid.add(createToggleWithDesc(enableIconsCheck, "Allows attaching custom color-coded symbols to tasks."), 1, 7);
        featuresGrid.add(createToggleWithDesc(enableZenModeCheck, "Adds a focus mode button that unlocks when the task paralysis threshold is met."), 0, 8);
        featuresGrid.add(createToggleWithDesc(enableOptionalTasksCheck, "Allows tasks that grant bonus points but do not count toward required daily totals."), 1, 8);
        featuresGrid.add(createToggleWithDesc(enableTaskStylingCheck, "Allows custom background, outline, and sidebox colors for individual tasks."), 0, 9);

        content.getChildren().add(featuresGrid);
        content.getChildren().add(new Separator());

        CheckBox enableStatsSystemCheck = new CheckBox("Enable Custom Stats");
        enableStatsSystemCheck.setSelected(config.isEnableStatsSystem());

        CheckBox enableLinkCardsCheck = new CheckBox("Enable Link Cards (Click to launch URL/App)");
        enableLinkCardsCheck.setSelected(config.isEnableLinkCards());

        CheckBox notesPageCheck = new CheckBox("Is Notes Page (Hides checkboxes & deadlines)");
        notesPageCheck.setSelected(config.isNotesPage());
        notesPageCheck.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");

        HBox extraBox = new HBox(20);
        extraBox.getChildren().addAll(enableStatsSystemCheck, enableLinkCardsCheck, notesPageCheck);
        content.getChildren().add(extraBox);

        presetBox.setOnAction(e -> {
            SectionConfig loadedPreset = presetBox.getValue();
            if (loadedPreset != null) {
                intervalSpinner.getValueFactory().setValue(loadedPreset.getResetIntervalHours());
                streakCheck.setSelected(loadedPreset.isHasStreak());
                allowManualArchiveCheck.setSelected(loadedPreset.isAllowManualArchiving());
                enableSubTasksCheck.setSelected(loadedPreset.isEnableSubTasks());
                showDateCheck.setSelected(loadedPreset.isShowDate());
                showPrefixCheck.setSelected(loadedPreset.isShowPrefix());
                showTagsCheck.setSelected(loadedPreset.isShowTags());
                enableScoreCheck.setSelected(loadedPreset.isEnableScore());
                enableLinksCheck.setSelected(loadedPreset.isEnableLinks());
                rewardsPageCheck.setSelected(loadedPreset.isRewardsPage());
                autoArchiveCheck.setSelected(loadedPreset.isAutoArchive());
                showPriorityCheck.setSelected(loadedPreset.isShowPriority());
                trackTimeCheck.setSelected(loadedPreset.isTrackTime());
                showTaskTypeCheck.setSelected(loadedPreset.isShowTaskType());
                favoriteCheck.setSelected(loadedPreset.isAllowFavorite());
                showAnalyticsCheck.setSelected(loadedPreset.isShowAnalytics());
                enableIconsCheck.setSelected(loadedPreset.isEnableIcons());
                enableZenModeCheck.setSelected(loadedPreset.isEnableZenMode());
                enableStatsSystemCheck.setSelected(loadedPreset.isEnableStatsSystem());
                enableLinkCardsCheck.setSelected(loadedPreset.isEnableLinkCards());
                notesPageCheck.setSelected(loadedPreset.isNotesPage());
                enableOptionalTasksCheck.setSelected(loadedPreset.isEnableOptionalTasks());
                enableTaskStylingCheck.setSelected(loadedPreset.isEnableTaskStyling());
            }
        });

        savePresetBtn.setOnAction(e -> {
            TextInputDialog nameDialog = new TextInputDialog("Custom Preset");
            nameDialog.setTitle("Save Preset");
            nameDialog.setHeaderText("Enter a name for this preset configuration:");
            TaskDialogs.styleDialog(nameDialog);

            nameDialog.showAndWait().ifPresent(presetName -> {
                SectionConfig newPreset = new SectionConfig(UUID.randomUUID().toString(), presetName);

                newPreset.setResetIntervalHours(intervalSpinner.getValue());
                newPreset.setHasStreak(streakCheck.isSelected());
                newPreset.setAllowManualArchiving(allowManualArchiveCheck.isSelected());
                newPreset.setEnableSubTasks(enableSubTasksCheck.isSelected());
                newPreset.setShowDate(showDateCheck.isSelected());
                newPreset.setShowPrefix(showPrefixCheck.isSelected());
                newPreset.setShowTags(showTagsCheck.isSelected());
                newPreset.setEnableScore(enableScoreCheck.isSelected());
                newPreset.setEnableLinks(enableLinksCheck.isSelected());
                newPreset.setRewardsPage(rewardsPageCheck.isSelected());
                newPreset.setAutoArchive(autoArchiveCheck.isSelected());
                newPreset.setShowPriority(showPriorityCheck.isSelected());
                newPreset.setTrackTime(trackTimeCheck.isSelected());
                newPreset.setShowTaskType(showTaskTypeCheck.isSelected());
                newPreset.setAllowFavorite(favoriteCheck.isSelected());
                newPreset.setShowAnalytics(showAnalyticsCheck.isSelected());
                newPreset.setEnableIcons(enableIconsCheck.isSelected());
                newPreset.setEnableZenMode(enableZenModeCheck.isSelected());
                newPreset.setEnableStatsSystem(enableStatsSystemCheck.isSelected());
                newPreset.setEnableLinkCards(enableLinkCardsCheck.isSelected());
                newPreset.setNotesPage(notesPageCheck.isSelected());
                newPreset.setEnableOptionalTasks(enableOptionalTasksCheck.isSelected());
                newPreset.setEnableTaskStyling(enableTaskStylingCheck.isSelected());

                appStats.getSectionPresets().add(newPreset);
                presetBox.getItems().add(newPreset);
                presetBox.setValue(newPreset);
                StorageManager.saveStats(appStats);
            });
        });

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String typedName = ((TextField)nameBox.getChildren().get(1)).getText().trim();

                if (isNew && typedName.isEmpty()) return;

                config.setName(typedName.isEmpty() ? "Unnamed Section" : typedName);
                config.setSidebarColor(toHexString(colorPicker.getValue()));
                config.setResetIntervalHours(intervalSpinner.getValue());

                config.setHasStreak(streakCheck.isSelected());
                config.setAllowManualArchiving(allowManualArchiveCheck.isSelected());
                config.setEnableSubTasks(enableSubTasksCheck.isSelected());
                config.setShowDate(showDateCheck.isSelected());
                config.setShowPrefix(showPrefixCheck.isSelected());
                config.setShowTags(showTagsCheck.isSelected());
                config.setEnableScore(enableScoreCheck.isSelected());
                config.setEnableLinks(enableLinksCheck.isSelected());
                config.setRewardsPage(rewardsPageCheck.isSelected());

                config.setAutoArchive(autoArchiveCheck.isSelected());
                config.setShowPriority(showPriorityCheck.isSelected());
                config.setTrackTime(trackTimeCheck.isSelected());
                config.setShowTaskType(showTaskTypeCheck.isSelected());
                config.setAllowFavorite(favoriteCheck.isSelected());
                config.setShowAnalytics(showAnalyticsCheck.isSelected());
                config.setEnableIcons(enableIconsCheck.isSelected());
                config.setEnableZenMode(enableZenModeCheck.isSelected());

                config.setEnableStatsSystem(enableStatsSystemCheck.isSelected());
                config.setEnableLinkCards(enableLinkCardsCheck.isSelected());
                config.setNotesPage(notesPageCheck.isSelected());
                config.setEnableOptionalTasks(enableOptionalTasksCheck.isSelected());
                config.setEnableTaskStyling(enableTaskStylingCheck.isSelected());

                if (isNew) {
                    appStats.getSections().add(config);
                }

                StorageManager.saveStats(appStats);
                refreshList();
                if(onSectionChanged != null) onSectionChanged.run();
                refreshCallback.run();
            }
        });
    }

    private String toHexString(Color color) {
        if (color == null) return null;
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}