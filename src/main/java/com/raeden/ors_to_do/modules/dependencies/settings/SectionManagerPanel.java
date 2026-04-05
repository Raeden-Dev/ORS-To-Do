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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SectionManagerPanel extends VBox {
    private VBox existingSectionsBox;
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private Runnable refreshCallback;
    private Runnable onSectionChanged;
    private final double BUTTON_WIDTH = 200.0;

    public SectionManagerPanel(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback, Runnable onSectionChanged) {
        super(15);
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.refreshCallback = refreshCallback;
        this.onSectionChanged = onSectionChanged;

        setStyle("-fx-border-color: #569CD6; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label sectionHeader = new Label("Manage Dynamic Sections");
        sectionHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #569CD6;");

        existingSectionsBox = new VBox(10);
        renderExistingSections();

        Button createSectionBtn = new Button("+ Create New Section");
        createSectionBtn.setPrefWidth(BUTTON_WIDTH);
        createSectionBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        createSectionBtn.setOnAction(e -> showSectionDialog(null));

        getChildren().addAll(sectionHeader, existingSectionsBox, new Separator(), createSectionBtn);
    }

    private VBox createToggleBox(Control control, String description) {
        VBox box = new VBox(2);
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px;");
        descLabel.setWrapText(true);
        box.getChildren().addAll(control, descLabel);
        return box;
    }

    private void renderExistingSections() {
        existingSectionsBox.getChildren().clear();

        for (int i = 0; i < appStats.getSections().size(); i++) {
            SectionConfig section = appStats.getSections().get(i);
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
                renderExistingSections();
                refreshCallback.run();
            });

            Button downBtn = new Button("▼");
            downBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
            downBtn.setDisable(index == appStats.getSections().size() - 1);
            downBtn.setOnAction(e -> {
                Collections.swap(appStats.getSections(), index, index + 1);
                StorageManager.saveStats(appStats);
                renderExistingSections();
                refreshCallback.run();
            });

            Button editBtn = new Button("Edit Config");
            editBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand;");
            editBtn.setOnAction(e -> showSectionDialog(section));

            Button removeBtn = new Button("Delete");
            removeBtn.setStyle("-fx-background-color: #552222; -fx-text-fill: white; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure? This will permanently delete the section '" + section.getName() + "' AND ALL TASKS inside it!", ButtonType.YES, ButtonType.NO);
                alert.setHeaderText("Delete Section?");
                TaskDialogs.styleDialog(alert);
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        appStats.getSections().remove(section);
                        globalDatabase.removeIf(t -> section.getId().equals(t.getSectionId()));
                        StorageManager.saveStats(appStats);
                        StorageManager.saveTasks(globalDatabase);

                        renderExistingSections();
                        onSectionChanged.run();
                    }
                });
            });

            btnBox.getChildren().addAll(upBtn, downBtn, editBtn, removeBtn);
            row.getChildren().addAll(colorRect, nameLabel, spacer, btnBox);
            existingSectionsBox.getChildren().add(row);
        }
    }

    private void showSectionDialog(SectionConfig config) {
        boolean isNew = (config == null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Create New Section" : "Edit Section: " + config.getName());
        TaskDialogs.styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(30); grid.setVgap(15);
        grid.setPadding(new Insets(10));

        TextField nameField = new TextField(isNew ? "" : config.getName());
        nameField.setPromptText("e.g. Reading List");
        grid.add(createToggleBox(nameField, "The name of your dynamic section."), 0, 0);

        ColorPicker sideColorPicker = new ColorPicker(Color.web(isNew ? "#569CD6" : config.getSidebarColor()));
        grid.add(createToggleBox(sideColorPicker, "The color of the sidebar label."), 1, 0);

        Spinner<Integer> resetSpinner = new Spinner<>(0, 8760, isNew ? 0 : config.getResetIntervalHours());
        resetSpinner.setEditable(true);
        grid.add(createToggleBox(resetSpinner, "Reset Interval (Hours). 0 = Never resets. 24 = Daily."), 0, 1);

        CheckBox chkStreak = new CheckBox("Enable Streak System");
        chkStreak.setSelected(!isNew && config.isHasStreak());
        chkStreak.disableProperty().bind(resetSpinner.valueProperty().isEqualTo(0));
        grid.add(createToggleBox(chkStreak, "Tracks consecutive completions. Requires a reset interval."), 1, 1);

        CheckBox chkArchive = new CheckBox("Allow Manual Archiving");
        chkArchive.setSelected(!isNew && config.isAllowArchive());
        grid.add(createToggleBox(chkArchive, "Enables right-click to send tasks to Archive."), 0, 2);

        CheckBox chkAutoArchive = new CheckBox("Auto-Archive Completed");
        chkAutoArchive.setSelected(!isNew && config.isAutoArchiveCompleted());
        grid.add(createToggleBox(chkAutoArchive, "Tasks are sent to archive the moment they are checked off."), 1, 2);

        CheckBox chkSubTasks = new CheckBox("Enable Sub-Tasks");
        chkSubTasks.setSelected(!isNew && config.isEnableSubTasks());
        grid.add(createToggleBox(chkSubTasks, "Allows creating nested to-do items inside a card."), 0, 3);

        CheckBox chkPriority = new CheckBox("Show Priority Toggles");
        chkPriority.setSelected(!isNew && config.isShowPriority());
        grid.add(createToggleBox(chkPriority, "Adds a priority ranking dropdown to each task."), 1, 3);

        CheckBox chkDate = new CheckBox("Show Creation Date");
        chkDate.setSelected(!isNew && config.isShowDate());
        grid.add(createToggleBox(chkDate, "Displays the exact date the task was generated."), 0, 4);

        CheckBox chkTime = new CheckBox("Track Focus Time");
        chkTime.setSelected(!isNew && config.isTrackTime());
        grid.add(createToggleBox(chkTime, "Links tasks to the Pomodoro Focus Hub timer."), 1, 4);

        CheckBox chkPrefix = new CheckBox("Enable Custom Prefixes");
        chkPrefix.setSelected(!isNew && config.isShowPrefix());
        grid.add(createToggleBox(chkPrefix, "Allows prefixing tags like [GYM] with custom colors."), 0, 5);

        CheckBox chkWorkType = new CheckBox("Enable Work Types");
        chkWorkType.setSelected(!isNew && config.isShowWorkType());
        grid.add(createToggleBox(chkWorkType, "Displays an editable string box for categorization."), 1, 5);

        CheckBox chkTags = new CheckBox("Enable Dynamic Filter Tags");
        chkTags.setSelected(!isNew && config.isShowTags());
        grid.add(createToggleBox(chkTags, "Auto-generates clickable sorting buttons at the top of the page."), 0, 6);

        CheckBox chkFavorite = new CheckBox("Enable Favorite System");
        chkFavorite.setSelected(!isNew && config.isAllowFavorite());
        grid.add(createToggleBox(chkFavorite, "Allows starring tasks for a golden border override."), 1, 6);

        CheckBox chkScore = new CheckBox("Enable Point System (Reward/Penalty)");
        chkScore.setSelected(!isNew && config.isEnableScore());
        grid.add(createToggleBox(chkScore, "Allows adding and earning score points for tasks."), 0, 7);

        CheckBox chkAnalytics = new CheckBox("Show Analytics Export");
        chkAnalytics.setSelected(!isNew && config.isShowAnalytics());
        grid.add(createToggleBox(chkAnalytics, "Displays a button to export an HTML graph of completed tasks."), 1, 7);

        CheckBox chkLinks = new CheckBox("Enable Task Links");
        chkLinks.setSelected(!isNew && config.isEnableLinks());
        grid.add(createToggleBox(chkLinks, "Allows attaching clickable URLs to tasks."), 0, 8);

        CheckBox chkIcons = new CheckBox("Enable Task Icons");
        chkIcons.setSelected(!isNew && config.isEnableIcons());
        grid.add(createToggleBox(chkIcons, "Allows attaching custom color-coded symbols to tasks."), 1, 8);

        CheckBox chkRewards = new CheckBox("Enable Rewards Shop Mode");
        chkRewards.setSelected(!isNew && config.isRewardsPage());
        chkRewards.setStyle("-fx-text-fill: #9CDCFE; -fx-font-weight: bold;");
        grid.add(createToggleBox(chkRewards, "Turns this list into a shop where items cost points instead of giving them."), 0, 9);

        CheckBox chkZen = new CheckBox("Enable Zen Mode");
        chkZen.setSelected(!isNew && config.isEnableZenMode());
        grid.add(createToggleBox(chkZen, "Adds a focus mode button that unlocks when the task paralysis threshold is met."), 1, 9);

        // --- FIXED: Safely check !isNew and actually ADD it to the layout Grid! ---
        CheckBox chkNotesPage = new CheckBox("Enable Notes Page Mode");
        chkNotesPage.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");
        chkNotesPage.setSelected(!isNew && config.isNotesPage());
        grid.add(createToggleBox(chkNotesPage, "Turns this section into a pinned notes board (no tasks/scores)."), 0, 10);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                SectionConfig target = isNew ? new SectionConfig(UUID.randomUUID().toString(), nameField.getText().trim()) : config;

                target.setName(nameField.getText().trim());
                target.setSidebarColor(toHexString(sideColorPicker.getValue()));
                target.setResetIntervalHours(resetSpinner.getValue());
                target.setHasStreak(chkStreak.isSelected() && resetSpinner.getValue() > 0);
                target.setAutoArchiveCompleted(chkAutoArchive.isSelected());
                target.setAllowArchive(chkArchive.isSelected());
                target.setEnableSubTasks(chkSubTasks.isSelected());
                target.setShowPriority(chkPriority.isSelected());
                target.setShowDate(chkDate.isSelected());
                target.setTrackTime(chkTime.isSelected());
                target.setShowPrefix(chkPrefix.isSelected());
                target.setShowWorkType(chkWorkType.isSelected());
                target.setShowTags(chkTags.isSelected());
                target.setEnableScore(chkScore.isSelected());
                target.setEnableLinks(chkLinks.isSelected());
                target.setAllowFavorite(chkFavorite.isSelected());
                target.setShowAnalytics(chkAnalytics.isSelected());
                target.setEnableIcons(chkIcons.isSelected());
                target.setRewardsPage(chkRewards.isSelected());
                target.setEnableZenMode(chkZen.isSelected());

                // --- FIXED: Save correctly to the target object ---
                target.setNotesPage(chkNotesPage.isSelected());

                if (isNew) {
                    appStats.getSections().add(target);
                }

                StorageManager.saveStats(appStats);
                renderExistingSections();
                onSectionChanged.run();
            }
        });
    }

    private String toHexString(Color color) {
        if (color == null) return null;
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}