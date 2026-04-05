package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.*;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskDialogs {

    public static final String[] ICON_LIST = {
            "None", "★", "☆", "⚡", "⚠", "⚙", "✉", "✎", "✔", "✖", "✚", "♫", "⚑", "⚐", "✂", "⌛", "⌚", "❀", "☾", "☁", "☂", "☃", "♛", "♚", "♞", "☯", "♦", "♣", "♠", "♥", "●", "■", "▲", "▼", "◆", "▶", "◀", "✦", "✧", "❂", "❖", "➤", "➥", "✓", "✗", "🔥", "🚀", "💡", "📌", "🏆"
    };

    public static void showAddSubTaskDialog(TaskItem task, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Sub-tasks");
        dialog.setHeaderText("Enter sub-tasks (one per line):");
        styleDialog(dialog);

        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(5);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-control-inner-background: #2D2D30; -fx-text-fill: white; -fx-prompt-text-fill: #858585;");

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && !textArea.getText().trim().isEmpty()) {
                String[] lines = textArea.getText().split("\\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        task.getSubTasks().add(new SubTask(line.trim()));
                    }
                }
                task.setExpanded(true); // Auto-expand to show new tasks
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }

    public static void showEditSubTaskDialog(SubTask subTask, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Sub-task");
        dialog.setHeaderText(null);
        styleDialog(dialog);

        TextField textField = new TextField(subTask.getTextContent());
        textField.setPrefWidth(300);

        textField.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: white; -fx-border-color: #3E3E42; -fx-border-radius: 3;");

        dialog.getDialogPane().setContent(textField);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.application.Platform.runLater(textField::requestFocus);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && !textField.getText().trim().isEmpty()) {
                subTask.setTextContent(textField.getText().trim());
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }

    public static void showEditDialog(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(config.isNotesPage() ? "Edit Note" : (config.isRewardsPage() ? "Edit Reward" : "Edit Task"));
        styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        int rowIdx = 0;

        // --- FIXED: Switched TextField to TextArea for multi-line support ---
        TextArea contentField = new TextArea(task.getTextContent());
        contentField.setMaxWidth(Double.MAX_VALUE);
        contentField.setWrapText(true);
        // Notes get a large box, standard tasks get a compact one
        contentField.setPrefRowCount(config.isNotesPage() ? 6 : 2);

        grid.add(new Label(config.isNotesPage() ? "Note Text:" : (config.isRewardsPage() ? "Reward Name:" : "Content:")), 0, rowIdx);
        grid.add(contentField, 1, rowIdx++);

        ColorPicker bgColorPicker = null;
        ColorPicker outlinePicker = null;
        ColorPicker sideboxPicker = null;

        if (config.isNotesPage()) {
            bgColorPicker = new ColorPicker();
            bgColorPicker.setMaxWidth(Double.MAX_VALUE);
            if (task.getColorHex() != null && !task.getColorHex().equals("transparent")) bgColorPicker.setValue(Color.web(task.getColorHex()));
            else bgColorPicker.setValue(Color.TRANSPARENT);

            outlinePicker = new ColorPicker();
            outlinePicker.setMaxWidth(Double.MAX_VALUE);
            if (task.getCustomOutlineColor() != null && !task.getCustomOutlineColor().equals("transparent")) outlinePicker.setValue(Color.web(task.getCustomOutlineColor()));
            else outlinePicker.setValue(Color.TRANSPARENT);

            sideboxPicker = new ColorPicker();
            sideboxPicker.setMaxWidth(Double.MAX_VALUE);
            if (task.getCustomSideboxColor() != null && !task.getCustomSideboxColor().equals("transparent")) sideboxPicker.setValue(Color.web(task.getCustomSideboxColor()));
            else sideboxPicker.setValue(Color.TRANSPARENT);

            grid.add(new Label("Background Color:"), 0, rowIdx); grid.add(bgColorPicker, 1, rowIdx++);
            grid.add(new Label("Outline Color:"), 0, rowIdx); grid.add(outlinePicker, 1, rowIdx++);
            grid.add(new Label("Sidebox Color:"), 0, rowIdx); grid.add(sideboxPicker, 1, rowIdx++);
        }

        MenuButton dependenciesMenu = new MenuButton("Dependencies (0)");
        dependenciesMenu.getStyleClass().add("custom-menu-btn");
        dependenciesMenu.setMaxWidth(Double.MAX_VALUE);
        List<String> selectedDeps = new ArrayList<>(task.getDependsOnTaskIds());
        int depCount = 0;

        for (TaskItem other : globalDatabase) {
            if (other.getId().equals(task.getId())) continue;
            if (other.isFinished() || other.isArchived()) continue;
            if (config.getId().equals(other.getSectionId())) {
                CheckBox cb = new CheckBox(other.getTextContent());
                cb.setStyle("-fx-text-fill: white;");
                cb.setSelected(selectedDeps.contains(other.getId()));
                if (cb.isSelected()) depCount++;
                cb.setOnAction(e -> {
                    if (cb.isSelected() && !selectedDeps.contains(other.getId())) selectedDeps.add(other.getId());
                    else if (!cb.isSelected()) selectedDeps.remove(other.getId());
                    dependenciesMenu.setText("Dependencies (" + selectedDeps.size() + ")");
                });

                CustomMenuItem item = new CustomMenuItem(cb);
                item.setHideOnClick(false);
                dependenciesMenu.getItems().add(item);
            }
        }
        dependenciesMenu.setText("Dependencies (" + depCount + ")");
        if (dependenciesMenu.getItems().isEmpty()) {
            CustomMenuItem emptyItem = new CustomMenuItem(new Label("No other active tasks"));
            emptyItem.setDisable(true);
            dependenciesMenu.getItems().add(emptyItem);
        }

        if (!config.isNotesPage()) {
            grid.add(new Label(config.isRewardsPage() ? "Unlock Condition:" : "Depends On:"), 0, rowIdx); grid.add(dependenciesMenu, 1, rowIdx++);
        }

        ComboBox<String> iconBox = null; ColorPicker iconColorPicker = null;
        if (config.isEnableIcons()) {
            iconBox = new ComboBox<>();
            iconBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(iconBox, Priority.ALWAYS);
            iconBox.getItems().addAll(ICON_LIST);
            iconBox.setValue(task.getIconSymbol() != null ? task.getIconSymbol() : "None");

            iconColorPicker = new ColorPicker(Color.web(task.getIconColor() != null ? task.getIconColor() : "#FFFFFF"));

            HBox iconRow = new HBox(10, iconBox, iconColorPicker);
            grid.add(new Label("Icon & Color:"), 0, rowIdx); grid.add(iconRow, 1, rowIdx++);
        }

        TextField prefixFieldEdit = null; ColorPicker preC = null;
        if (config.isShowPrefix()) {
            prefixFieldEdit = new TextField(task.getPrefix());
            prefixFieldEdit.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(prefixFieldEdit, Priority.ALWAYS);

            preC = new ColorPicker(Color.web(task.getPrefixColor() != null ? task.getPrefixColor() : "#4EC9B0"));

            HBox prefixRow = new HBox(10, prefixFieldEdit, preC);
            grid.add(new Label("Prefix & Color:"), 0, rowIdx); grid.add(prefixRow, 1, rowIdx++);
        }

        if (config.isEnableIcons() || config.isShowPrefix()) {
            Button randomBtn = new Button("🎲 Randomize Appearance");
            randomBtn.setMaxWidth(Double.MAX_VALUE);

            ComboBox<String> finalRndIconBox = iconBox;
            ColorPicker finalRndIconColor = iconColorPicker;
            ColorPicker finalRndPrefixColor = preC;

            randomBtn.setOnAction(e -> {
                java.util.Random rand = new java.util.Random();
                double hue = rand.nextDouble() * 360.0;

                if (finalRndIconBox != null) {
                    finalRndIconBox.setValue(ICON_LIST[rand.nextInt(ICON_LIST.length - 1) + 1]);
                }
                if (finalRndIconColor != null) {
                    finalRndIconColor.setValue(Color.hsb(hue, 0.5, 0.95));
                }
                if (finalRndPrefixColor != null) {
                    finalRndPrefixColor.setValue(Color.hsb(hue, 0.7, 0.55));
                }
            });
            grid.add(randomBtn, 1, rowIdx++);
        }

        ComboBox<CustomPriority> prioBoxEdit = null;
        if (config.isShowPriority() && !config.isNotesPage()) {
            prioBoxEdit = new ComboBox<>();
            prioBoxEdit.setMaxWidth(Double.MAX_VALUE);
            prioBoxEdit.getItems().addAll(appStats.getCustomPriorities());
            prioBoxEdit.setValue(task.getPriority());
            setupPriorityBoxColors(prioBoxEdit);
            grid.add(new Label(config.isRewardsPage() ? "Reward Tier:" : "Priority:"), 0, rowIdx); grid.add(prioBoxEdit, 1, rowIdx++);
        }

        TextField workTypeField = null;
        if (config.isShowWorkType()) {
            workTypeField = new TextField(task.getWorkType());
            workTypeField.setMaxWidth(Double.MAX_VALUE);
            grid.add(new Label("Category:"), 0, rowIdx); grid.add(workTypeField, 1, rowIdx++);
        }

        DatePicker datePicker = new DatePicker();
        TextField timePicker = new TextField();
        TextField maxCountField = new TextField(String.valueOf(task.getMaxCount()));
        TextField costField = new TextField(String.valueOf(task.getCostPoints()));
        TextField rewardField = new TextField(String.valueOf(task.getRewardPoints()));
        TextField penaltyField = new TextField(String.valueOf(task.getPenaltyPoints()));

        if (!config.isNotesPage()) {
            datePicker.setMaxWidth(Double.MAX_VALUE);
            if (task.getDeadline() != null) datePicker.setValue(task.getDeadline().toLocalDate());

            timePicker.setMaxWidth(Double.MAX_VALUE);
            timePicker.setPromptText("HH:mm (24h)");
            if (task.getDeadline() != null) timePicker.setText(task.getDeadline().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

            grid.add(new Label(config.isRewardsPage() ? "Available Until:" : "Deadline Date:"), 0, rowIdx); grid.add(datePicker, 1, rowIdx++);
            grid.add(new Label("Time:"), 0, rowIdx); grid.add(timePicker, 1, rowIdx++);

            grid.add(new Separator(), 0, rowIdx, 2, 1); rowIdx++;

            maxCountField.setMaxWidth(Double.MAX_VALUE);
            maxCountField.setPromptText(config.isRewardsPage() ? "0 = Infinite purchases" : "0 = Standard task (No counter)");
            grid.add(new Label(config.isRewardsPage() ? "Max Purchases:" : "Counter Goal (0=Off):"), 0, rowIdx); grid.add(maxCountField, 1, rowIdx++);

            costField.setMaxWidth(Double.MAX_VALUE);
            rewardField.setMaxWidth(Double.MAX_VALUE);
            penaltyField.setMaxWidth(Double.MAX_VALUE);

            if (config.isRewardsPage()) {
                grid.add(new Label("Reward Cost (Points):"), 0, rowIdx); grid.add(costField, 1, rowIdx++);
            } else if (config.isEnableScore()) {
                grid.add(new Label("Reward Points:"), 0, rowIdx); grid.add(rewardField, 1, rowIdx++);
                grid.add(new Label("Missed Penalty:"), 0, rowIdx); grid.add(penaltyField, 1, rowIdx++);
            }
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ColorPicker finalBgColorPicker = bgColorPicker;
        ColorPicker finalOutlinePicker = outlinePicker;
        ColorPicker finalSideboxPicker = sideboxPicker;

        TextField finalPrefixFieldEdit = prefixFieldEdit; ColorPicker finalPreC = preC;
        ComboBox<CustomPriority> finalPrioBoxEdit = prioBoxEdit; TextField finalWorkTypeField = workTypeField;
        ComboBox<String> finalIconBox = iconBox; ColorPicker finalIconColorPicker = iconColorPicker;

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                task.setTextContent(contentField.getText().trim());

                if (config.isNotesPage()) {
                    if (finalBgColorPicker != null) task.setColorHex(toHexString(finalBgColorPicker.getValue()));
                    if (finalOutlinePicker != null) task.setCustomOutlineColor(toHexString(finalOutlinePicker.getValue()));
                    if (finalSideboxPicker != null) task.setCustomSideboxColor(toHexString(finalSideboxPicker.getValue()));
                } else {
                    task.setDependsOnTaskIds(selectedDeps);
                    if (config.isShowPriority() && finalPrioBoxEdit != null) task.setPriority(finalPrioBoxEdit.getValue());

                    if (datePicker.getValue() != null) {
                        try {
                            LocalTime time = LocalTime.MIDNIGHT;
                            if (!timePicker.getText().trim().isEmpty()) time = LocalTime.parse(timePicker.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                            task.setDeadline(LocalDateTime.of(datePicker.getValue(), time));
                        } catch (Exception ex) { task.setDeadline(LocalDateTime.of(datePicker.getValue(), LocalTime.MIDNIGHT)); }
                    } else task.setDeadline(null);

                    int maxC = 0;
                    try { maxC = Math.max(0, Integer.parseInt(maxCountField.getText().trim())); } catch (Exception ignore) {}
                    task.setMaxCount(maxC);
                    task.setCounterMode(maxC > 0);

                    if (config.isRewardsPage()) {
                        try { task.setCostPoints(Math.max(0, Integer.parseInt(costField.getText().trim()))); } catch (Exception ignore) {}
                    } else if (config.isEnableScore()) {
                        try { task.setRewardPoints(Math.max(0, Integer.parseInt(rewardField.getText().trim()))); } catch (Exception ignore) {}
                        try { task.setPenaltyPoints(Math.max(0, Integer.parseInt(penaltyField.getText().trim()))); } catch (Exception ignore) {}
                    }
                }

                if (config.isEnableIcons() && finalIconBox != null) {
                    task.setIconSymbol(finalIconBox.getValue());
                    task.setIconColor(toHexString(finalIconColorPicker.getValue()));
                }
                if (config.isShowPrefix() && finalPrefixFieldEdit != null) {
                    task.setPrefix(finalPrefixFieldEdit.getText().trim());
                    task.setPrefixColor(toHexString(finalPreC.getValue()));
                }
                if (config.isShowWorkType() && finalWorkTypeField != null) task.setWorkType(finalWorkTypeField.getText().trim());

                StorageManager.saveTasks(globalDatabase); onUpdate.run();
            }
        });
    }

    public static void showLinkDialog(TaskItem task, TaskLink existingLink, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingLink == null ? "Add Link" : "Edit Link");
        styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField nameField = new TextField(existingLink == null ? "" : existingLink.getName());
        TextField urlField = new TextField(existingLink == null ? "" : existingLink.getUrl());
        grid.add(new Label("Link Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("URL:"), 0, 1); grid.add(urlField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !urlField.getText().trim().isEmpty()) {
                String name = nameField.getText().trim().isEmpty() ? urlField.getText().trim() : nameField.getText().trim();
                if (existingLink == null) task.getTaskLinks().add(new TaskLink(name, urlField.getText().trim()));
                else { existingLink.setName(name); existingLink.setUrl(urlField.getText().trim()); }
                task.setExpanded(true); StorageManager.saveTasks(globalDatabase); onUpdate.run();
            }
        });
    }

    public static void showCreditsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About & Credits");
        alert.setHeaderText("Task Tracker Suite");
        alert.setContentText(
                "Developed for One Raid Studio.\n\n" +
                        "Designed to maximize productivity, eliminate task paralysis, and gamify workflow. Thank you for using the application!"
        );
        styleDialog(alert);
        alert.show();
    }

    public static void showHelpDialog(AppStats stats) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Application Help Guide");
        dialog.setHeaderText("How to use the application:");
        styleDialog(dialog);

        VBox contentBox = new VBox(15);
        contentBox.setStyle("-fx-padding: 10;");
        contentBox.setPrefWidth(450);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.getStylesheets().add("data:text/css;base64," +
                java.util.Base64.getEncoder().encodeToString(".scroll-pane > .viewport { -fx-background-color: transparent; } .scroll-bar:vertical { -fx-opacity: 0.7; }".getBytes()));

        contentBox.getChildren().addAll(
                createHelpCard("📊 Analytics", "Track completion streaks, total tasks done, and productivity statistics in the main dashboard."),
                createHelpCard("☯ Zen Mode", "Access Zen Mode from a section dashboard to focus on a single high-priority task. " +
                        "It becomes available when tasks exceed your threshold (currently: " + stats.getZenModeThreshold() + ")."),
                createHelpCard("⏱ Pomodoro Timer", "Located in Focus Hub. Link the timer to an active, time-tracked task to automatically " +
                        "log your work duration upon session completion."),
                createHelpCard("📌 Sub-Task Locks", "If a task has sub-tasks, the main completion button will lock until all sub-tasks are completed."),
                createHelpCard("💎 Rewards Shop", "Turn any section into a 'Rewards Page' via Section Manager. Assign cost points to items and 'Buy' them using your global score.")
        );

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
    }

    private static VBox createHelpCard(String title, String description) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #2D2D30; -fx-padding: 10; -fx-border-color: #3E3E42; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label tLabel = new Label(title);
        tLabel.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label dLabel = new Label(description);
        dLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 13px;");
        dLabel.setWrapText(true);

        card.getChildren().addAll(tLabel, dLabel);
        return card;
    }

    public static void styleDialog(Dialog<?> dialog) {
        // --- FIXED: Injected .text-area CSS to ensure the multi-line input perfectly matches dark theme ---
        String css = ".dialog-pane { -fx-background-color: #1E1E1E; -fx-border-color: #3E3E42; -fx-border-width: 1; } " +
                ".dialog-pane > *.content.label { -fx-text-fill: #E0E0E0; } " +
                ".dialog-pane .header-panel { -fx-background-color: #2D2D30; -fx-border-bottom-color: #3E3E42; -fx-border-width: 0 0 1 0; } " +
                ".dialog-pane .header-panel .label { -fx-text-fill: #569CD6; -fx-font-weight: bold; } " +
                ".button { -fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; } " +
                ".button:hover { -fx-background-color: #569CD6; -fx-border-color: #569CD6; } " +
                ".button:default { -fx-background-color: #0E639C; -fx-border-color: #0E639C; } " +
                ".button:default:hover { -fx-background-color: #1177BB; } " +
                ".text-field, .text-area, .combo-box { -fx-background-color: #2D2D30; -fx-control-inner-background: #2D2D30; -fx-text-fill: white; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; } " +
                ".text-area .content { -fx-background-color: #2D2D30; } " +
                ".combo-box .list-cell { -fx-text-fill: white; } " +
                ".combo-box-popup .list-view { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                ".combo-box-popup .list-view .list-cell { -fx-background-color: #2D2D30; -fx-text-fill: white; } " +
                ".combo-box-popup .list-view .list-cell:filled:hover, .combo-box-popup .list-view .list-cell:filled:selected { -fx-background-color: #569CD6; -fx-text-fill: white; } " +
                ".color-picker { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                ".color-picker .label { -fx-text-fill: white; } " +
                ".label, .check-box { -fx-text-fill: #E0E0E0; } " +
                ".check-box .box { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                ".check-box:selected .mark { -fx-background-color: white; } " +
                ".custom-menu-btn { -fx-background-color: #2D2D30; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; } " +
                ".custom-menu-btn .label { -fx-text-fill: white; } " +
                ".context-menu { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                ".menu-item { -fx-background-color: #2D2D30; } " +
                ".menu-item:hover, .menu-item:focused { -fx-background-color: #569CD6; } " +
                ".menu-item .label { -fx-text-fill: white; }";

        String b64 = java.util.Base64.getEncoder().encodeToString(css.getBytes());
        dialog.getDialogPane().getStylesheets().add("data:text/css;base64," + b64);
        dialog.getDialogPane().setStyle("-fx-background-color: #1E1E1E;");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, oldWin, newWin) -> {
                    if (newWin instanceof javafx.stage.Stage) {
                        ((javafx.stage.Stage) newWin).setAlwaysOnTop(true);
                    }
                });
            }
        });

        if (dialogPane.getScene() != null && dialogPane.getScene().getWindow() instanceof javafx.stage.Stage) {
            ((javafx.stage.Stage) dialogPane.getScene().getWindow()).setAlwaysOnTop(true);
        }
    }

    public static void setupPriorityBoxColors(ComboBox<CustomPriority> box) {
        box.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(CustomPriority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item.getName()); setStyle("-fx-text-fill: " + item.getColorHex() + "; -fx-font-weight: bold;"); }
            }
        });
        box.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(CustomPriority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item.getName()); setStyle("-fx-text-fill: " + item.getColorHex() + "; -fx-font-weight: bold;"); }
            }
        });
    }

    public static String toHexString(Color color) {
        if (color == null) return null;
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}