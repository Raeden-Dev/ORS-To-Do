package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

        // --- Text & General Settings ---
        VBox textSettings = new VBox(15);
        textSettings.setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label textHeader = new Label("General Customization");
        textHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #CCCCCC;");

        GridPane textGrid = new GridPane();
        textGrid.setHgap(15); textGrid.setVgap(10);

        TextField editMenuField = new TextField(appStats.getEditMenuText()); editMenuField.setPromptText("Default: Edit Task");
        TextField archiveMenuField = new TextField(appStats.getArchiveMenuText()); archiveMenuField.setPromptText("Default: Archive Task");
        TextField deleteMenuField = new TextField(appStats.getDeleteMenuText()); deleteMenuField.setPromptText("Default: Delete");

        Spinner<Integer> fontSizeSpinner = new Spinner<>(10, 36, appStats.getTaskFontSize());
        fontSizeSpinner.setEditable(true);

        textGrid.add(new Label("Edit Menu Text:"), 0, 0); textGrid.add(editMenuField, 1, 0);
        textGrid.add(new Label("Archive Menu Text:"), 0, 1); textGrid.add(archiveMenuField, 1, 1);
        textGrid.add(new Label("Delete Menu Text:"), 0, 2); textGrid.add(deleteMenuField, 1, 2);
        textGrid.add(new Label("Task Font Size:"), 0, 3); textGrid.add(fontSizeSpinner, 1, 3);

        // --- NEW: Background Checkbox ---
        CheckBox runInBackgroundCheck = new CheckBox("Run app in background (System Tray) when closed");
        runInBackgroundCheck.setSelected(appStats.isRunInBackground());
        runInBackgroundCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        textGrid.add(runInBackgroundCheck, 0, 4, 2, 1); // Spans across 2 columns

        TextField quickNavField = new TextField(appStats.getNavQuickText()); quickNavField.setPromptText("Default: Quick To-Do");
        TextField dailyNavField = new TextField(appStats.getNavDailyText()); dailyNavField.setPromptText("Default: Daily To-Do");
        TextField workNavField = new TextField(appStats.getNavWorkText()); workNavField.setPromptText("Default: Work List");
        TextField focusNavField = new TextField(appStats.getNavFocusText()); focusNavField.setPromptText("Default: Focus Hub");
        TextField archiveNavField = new TextField(appStats.getNavArchiveText()); archiveNavField.setPromptText("Default: Archived");
        TextField settingsNavField = new TextField(appStats.getNavSettingsText()); settingsNavField.setPromptText("Default: Settings");

        textGrid.add(new Label("Quick To-Do Nav:"), 2, 0); textGrid.add(quickNavField, 3, 0);
        textGrid.add(new Label("Daily To-Do Nav:"), 2, 1); textGrid.add(dailyNavField, 3, 1);
        textGrid.add(new Label("Work List Nav:"), 2, 2); textGrid.add(workNavField, 3, 2);
        textGrid.add(new Label("Focus Hub Nav:"), 2, 3); textGrid.add(focusNavField, 3, 3);
        textGrid.add(new Label("Archived Nav:"), 2, 4); textGrid.add(archiveNavField, 3, 4);
        textGrid.add(new Label("Settings Nav:"), 2, 5); textGrid.add(settingsNavField, 3, 5);

        Button saveTextBtn = new Button("Save Changes");
        saveTextBtn.setPrefWidth(BUTTON_WIDTH);
        saveTextBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        saveTextBtn.setOnAction(e -> {
            appStats.setEditMenuText(editMenuField.getText().trim().isEmpty() ? "Edit Task" : editMenuField.getText().trim());
            appStats.setArchiveMenuText(archiveMenuField.getText().trim().isEmpty() ? "Archive Task" : archiveMenuField.getText().trim());
            appStats.setDeleteMenuText(deleteMenuField.getText().trim().isEmpty() ? "Delete" : deleteMenuField.getText().trim());
            appStats.setTaskFontSize(fontSizeSpinner.getValue());
            appStats.setRunInBackground(runInBackgroundCheck.isSelected()); // Save Background Preference

            appStats.setNavQuickText(quickNavField.getText().trim().isEmpty() ? "Quick To-Do" : quickNavField.getText().trim());
            appStats.setNavDailyText(dailyNavField.getText().trim().isEmpty() ? "Daily To-Do" : dailyNavField.getText().trim());
            appStats.setNavWorkText(workNavField.getText().trim().isEmpty() ? "Work List" : workNavField.getText().trim());
            appStats.setNavFocusText(focusNavField.getText().trim().isEmpty() ? "Focus Hub" : focusNavField.getText().trim());
            appStats.setNavArchiveText(archiveNavField.getText().trim().isEmpty() ? "Archived" : archiveNavField.getText().trim());
            appStats.setNavSettingsText(settingsNavField.getText().trim().isEmpty() ? "Settings" : settingsNavField.getText().trim());

            StorageManager.saveStats(appStats);
            refreshCallback.run();
        });

        textSettings.getChildren().addAll(textHeader, textGrid, saveTextBtn);


        // --- CHANGED: Renamed Section ---
        VBox templateSettings = new VBox(15);
        templateSettings.setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label templateHeader = new Label("Daily Tasks to Add");
        templateHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #CCCCCC;");

        existingTemplatesBox = new VBox(10);
        renderExistingTemplates(appStats, refreshCallback);

        HBox templateInput = new HBox(10);
        TextField tempPrefix = new TextField(); tempPrefix.setPromptText("[Prefix]"); tempPrefix.setPrefWidth(80);
        TextField tempText = new TextField(); tempText.setPromptText("Task Content (e.g. Push Day)");
        HBox.setHgrow(tempText, Priority.ALWAYS);

        Button addTempBtn = new Button("Add Task");
        addTempBtn.setPrefWidth(BUTTON_WIDTH);
        addTempBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-border-color: #555555; -fx-cursor: hand;");
        addTempBtn.setOnAction(e -> {
            if(!tempText.getText().isEmpty()) {
                String cleanPrefix = tempPrefix.getText().trim().toUpperCase();
                if (!cleanPrefix.isEmpty()) {
                    if (!cleanPrefix.startsWith("[")) cleanPrefix = "[" + cleanPrefix;
                    if (!cleanPrefix.endsWith("]")) cleanPrefix = cleanPrefix + "]";
                }
                appStats.getBaseDailies().add(new AppStats.DailyTemplate(cleanPrefix, tempText.getText().trim()));
                StorageManager.saveStats(appStats);
                renderExistingTemplates(appStats, refreshCallback);
                tempPrefix.clear(); tempText.clear();
            }
        });
        templateInput.getChildren().addAll(tempPrefix, tempText);
        templateSettings.getChildren().addAll(templateHeader, existingTemplatesBox, new Separator(), templateInput, addTempBtn);


        // --- Custom Priorities Section ---
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
                String hex = String.format("#%02X%02X%02X",
                        (int)(colorPicker.getValue().getRed()*255),
                        (int)(colorPicker.getValue().getGreen()*255),
                        (int)(colorPicker.getValue().getBlue()*255));
                appStats.getCustomPriorities().add(new TaskItem.CustomPriority(prioName.getText(), hex));
                StorageManager.saveStats(appStats);
                renderExistingPriorities(appStats, refreshCallback);
                refreshCallback.run();
                prioName.clear();
            }
        });
        prioInput.getChildren().addAll(prioName, colorPicker);
        prioSettings.getChildren().addAll(prioHeader, existingPriosBox, new Separator(), prioInput, addPrioBtn);


        // --- Danger Zone Section ---
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

        // --- NEW: Reset Streak Button ---
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
        wipeGrid.add(resetStreakBtn, 0, 2); // Added below wipes

        dangerZone.getChildren().addAll(dangerLabel, wipeGrid);

        contentBox.getChildren().addAll(header, textSettings, templateSettings, prioSettings, dangerZone);
        setContent(contentBox);
    }

    private void renderExistingTemplates(AppStats appStats, Runnable refreshCallback) {
        existingTemplatesBox.getChildren().clear();

        for (AppStats.DailyTemplate temp : appStats.getBaseDailies()) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);

            Label prefixLabel = new Label(temp.getPrefix() != null ? temp.getPrefix() : "");
            prefixLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold; -fx-font-size: 14px;");
            prefixLabel.setPrefWidth(80);

            Label textLabel = new Label(temp.getText());
            textLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px;");
            HBox.setHgrow(textLabel, Priority.ALWAYS);

            Button removeBtn = new Button("Remove");
            removeBtn.setStyle("-fx-background-color: #552222; -fx-text-fill: white; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                appStats.getBaseDailies().remove(temp);
                StorageManager.saveStats(appStats);
                renderExistingTemplates(appStats, refreshCallback);
            });

            row.getChildren().addAll(prefixLabel, textLabel, removeBtn);
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
}