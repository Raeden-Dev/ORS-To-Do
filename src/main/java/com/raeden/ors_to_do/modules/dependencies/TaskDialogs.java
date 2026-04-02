package com.raeden.ors_to_do.modules.dependencies;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskDialogs {

    public static void showAddSubTaskDialog(TaskItem task, List<TaskItem> globalDatabase, Runnable onUpdate) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Sub-task");
        dialog.setHeaderText("Create a new sub-task for: " + task.getTextContent());
        dialog.showAndWait().ifPresent(text -> {
            if (!text.trim().isEmpty()) {
                task.getSubTasks().add(new TaskItem.SubTask(text.trim()));
                task.setExpanded(true);
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }

    public static void showLinkDialog(TaskItem task, TaskItem.TaskLink existingLink, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingLink == null ? "Add Link" : "Edit Link");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField nameField = new TextField(existingLink == null ? "" : existingLink.getName());
        nameField.setPromptText("e.g. Project Doc");

        TextField urlField = new TextField(existingLink == null ? "" : existingLink.getUrl());
        urlField.setPromptText("e.g. https://...");

        grid.add(new Label("Link Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("URL:"), 0, 1);
        grid.add(urlField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !urlField.getText().trim().isEmpty()) {
                String name = nameField.getText().trim();
                // --- FIX: Default to the raw URL if no name is provided ---
                if (name.isEmpty()) name = urlField.getText().trim();

                if (existingLink == null) {
                    task.getTaskLinks().add(new TaskItem.TaskLink(name, urlField.getText().trim()));
                } else {
                    existingLink.setName(name);
                    existingLink.setUrl(urlField.getText().trim());
                }

                task.setExpanded(true);
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }

    public static void showEditDialog(TaskItem task, AppStats.SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10);

        int rowIdx = 0;
        TextField contentField = new TextField(task.getTextContent());
        grid.add(new Label("Content:"), 0, rowIdx); grid.add(contentField, 1, rowIdx++);

        TextField prefixFieldEdit = null;
        ColorPicker preC = null;
        if (config.isShowPrefix()) {
            prefixFieldEdit = new TextField(task.getPrefix());
            preC = new ColorPicker(Color.web(task.getPrefixColor() != null ? task.getPrefixColor() : "#4EC9B0"));
            grid.add(new Label("Prefix:"), 0, rowIdx); grid.add(prefixFieldEdit, 1, rowIdx++);
            grid.add(new Label("Prefix Color:"), 0, rowIdx); grid.add(preC, 1, rowIdx++);
        }

        ComboBox<TaskItem.CustomPriority> prioBoxEdit = null;
        if (config.isShowPriority()) {
            prioBoxEdit = new ComboBox<>();
            prioBoxEdit.getItems().addAll(appStats.getCustomPriorities());
            prioBoxEdit.setValue(task.getPriority());
            setupPriorityBoxColors(prioBoxEdit);
            grid.add(new Label("Priority:"), 0, rowIdx); grid.add(prioBoxEdit, 1, rowIdx++);
        }

        TextField workTypeField = null;
        if (config.isShowWorkType()) {
            workTypeField = new TextField(task.getWorkType());
            grid.add(new Label("Work Type:"), 0, rowIdx); grid.add(workTypeField, 1, rowIdx++);
        }

        DatePicker datePicker = new DatePicker();
        if (task.getDeadline() != null) datePicker.setValue(task.getDeadline().toLocalDate());
        TextField timePicker = new TextField();
        timePicker.setPromptText("HH:mm (24h)");
        if (task.getDeadline() != null) timePicker.setText(task.getDeadline().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        grid.add(new Label("Deadline Date:"), 0, rowIdx); grid.add(datePicker, 1, rowIdx++);
        grid.add(new Label("Deadline Time:"), 0, rowIdx); grid.add(timePicker, 1, rowIdx++);

        grid.add(new Separator(), 0, rowIdx, 2, 1); rowIdx++;

        CheckBox chkCounter = new CheckBox("Enable Counter Mode");
        chkCounter.setSelected(task.isCounterMode());
        TextField maxCountField = new TextField(String.valueOf(task.getMaxCount()));
        maxCountField.setPromptText("0 = Infinite");
        grid.add(chkCounter, 0, rowIdx); grid.add(maxCountField, 1, rowIdx++);

        TextField rewardField = new TextField(String.valueOf(task.getRewardPoints()));
        TextField penaltyField = new TextField(String.valueOf(task.getPenaltyPoints()));

        if (config.isEnableScore()) {
            grid.add(new Label("Reward Points:"), 0, rowIdx); grid.add(rewardField, 1, rowIdx++);
            grid.add(new Label("Missed Deadline Penalty:"), 0, rowIdx); grid.add(penaltyField, 1, rowIdx++);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField finalPrefixFieldEdit = prefixFieldEdit;
        ColorPicker finalPreC = preC;
        ComboBox<TaskItem.CustomPriority> finalPrioBoxEdit = prioBoxEdit;
        TextField finalWorkTypeField = workTypeField;

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                task.setTextContent(contentField.getText().trim());
                if (config.isShowPrefix() && finalPrefixFieldEdit != null) {
                    task.setPrefix(finalPrefixFieldEdit.getText().trim());
                    task.setPrefixColor(toHexString(finalPreC.getValue()));
                }
                if (config.isShowPriority() && finalPrioBoxEdit != null) task.setPriority(finalPrioBoxEdit.getValue());
                if (config.isShowWorkType() && finalWorkTypeField != null) task.setWorkType(finalWorkTypeField.getText().trim());

                if (datePicker.getValue() != null) {
                    try {
                        LocalTime time = LocalTime.MIDNIGHT;
                        if (!timePicker.getText().trim().isEmpty()) {
                            time = LocalTime.parse(timePicker.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                        }
                        task.setDeadline(LocalDateTime.of(datePicker.getValue(), time));
                    } catch (Exception ex) {
                        task.setDeadline(LocalDateTime.of(datePicker.getValue(), LocalTime.MIDNIGHT));
                    }
                } else {
                    task.setDeadline(null);
                }

                task.setCounterMode(chkCounter.isSelected());
                try { task.setMaxCount(Math.max(0, Integer.parseInt(maxCountField.getText().trim()))); } catch (Exception ignore) {}

                if (config.isEnableScore()) {
                    try { task.setRewardPoints(Math.max(0, Integer.parseInt(rewardField.getText().trim()))); } catch (Exception ignore) {}
                    try { task.setPenaltyPoints(Math.max(0, Integer.parseInt(penaltyField.getText().trim()))); } catch (Exception ignore) {}
                }

                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }

    public static void setupPriorityBoxColors(ComboBox<TaskItem.CustomPriority> box) {
        box.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TaskItem.CustomPriority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item.getName()); setStyle("-fx-text-fill: " + item.getColorHex() + "; -fx-font-weight: bold;"); }
            }
        });
        box.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TaskItem.CustomPriority item, boolean empty) {
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