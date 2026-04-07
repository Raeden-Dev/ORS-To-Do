package com.raeden.ors_to_do.modules.dependencies.ui.forms;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskMetaForm {
    private List<String> selectedDeps;
    private ComboBox<CustomPriority> prioBoxEdit;
    private TextField taskTypeField;
    private DatePicker datePicker;
    private TextField timePicker;
    private TextField targetTimeField; // NEW

    public void buildUI(GridPane grid, AtomicInteger rowIdx, TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> db) {
        selectedDeps = new ArrayList<>(task.getDependsOnTaskIds());
        MenuButton dependenciesMenu = buildDependenciesMenu(task, config, db);
        grid.add(new Label((config != null && config.isRewardsPage()) ? "Unlock Condition:" : "Depends On:"), 0, rowIdx.get());
        grid.add(dependenciesMenu, 1, rowIdx.getAndIncrement());

        if ((config == null || (config.isShowPriority() && !config.isNotesPage())) && !task.isOptional()) {
            prioBoxEdit = new ComboBox<>();
            prioBoxEdit.setMaxWidth(Double.MAX_VALUE);
            prioBoxEdit.getItems().addAll(appStats.getCustomPriorities());
            prioBoxEdit.setValue(task.getPriority());
            TaskDialogs.setupPriorityBoxColors(prioBoxEdit);
            grid.add(new Label((config != null && config.isRewardsPage()) ? "Reward Tier:" : "Priority:"), 0, rowIdx.get());
            grid.add(prioBoxEdit, 1, rowIdx.getAndIncrement());
        }

        if (config == null || config.isShowTaskType()) {
            taskTypeField = new TextField(task.getTaskType() != null ? task.getTaskType() : "");
            taskTypeField.setMaxWidth(Double.MAX_VALUE);
            grid.add(new Label("Category:"), 0, rowIdx.get());
            grid.add(taskTypeField, 1, rowIdx.getAndIncrement());
        }

        // --- NEW: Timed Tasks Input ---
        if (config == null || config.isEnableTimedTasks()) {
            targetTimeField = new TextField();
            targetTimeField.setMaxWidth(Double.MAX_VALUE);
            targetTimeField.setPromptText("Minutes (0 = Off)");
            if (task.getTargetTimeMinutes() > 0) targetTimeField.setText(String.valueOf(task.getTargetTimeMinutes()));

            grid.add(new Label("Required Focus Time (m):"), 0, rowIdx.get());
            grid.add(targetTimeField, 1, rowIdx.getAndIncrement());
        }

        datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        if (task.getDeadline() != null) datePicker.setValue(task.getDeadline().toLocalDate());

        timePicker = new TextField();
        timePicker.setMaxWidth(Double.MAX_VALUE);
        timePicker.setPromptText("HH:mm (24h)");
        if (task.getDeadline() != null) timePicker.setText(task.getDeadline().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        timePicker.setDisable(datePicker.getValue() == null);
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> timePicker.setDisable(newVal == null));

        grid.add(new Label((config != null && config.isRewardsPage()) ? "Available Until:" : "Deadline Date:"), 0, rowIdx.get());
        grid.add(datePicker, 1, rowIdx.getAndIncrement());
        grid.add(new Label("Exact Time:"), 0, rowIdx.get());
        grid.add(timePicker, 1, rowIdx.getAndIncrement());
        grid.add(new Separator(), 0, rowIdx.get(), 2, 1); rowIdx.getAndIncrement();
    }

    public void applyTo(TaskItem task) {
        task.setDependsOnTaskIds(selectedDeps);
        if (prioBoxEdit != null) task.setPriority(prioBoxEdit.getValue());
        else if (task.isOptional()) task.setPriority(null);
        if (taskTypeField != null) task.setTaskType(taskTypeField.getText().trim());

        // --- NEW: Save Timed Task Target ---
        if (targetTimeField != null) {
            try { task.setTargetTimeMinutes(Math.max(0, Integer.parseInt(targetTimeField.getText().trim()))); }
            catch (Exception e) { task.setTargetTimeMinutes(0); }
        }

        if (datePicker.getValue() != null) {
            try {
                LocalTime time = LocalTime.MIDNIGHT;
                if (!timePicker.getText().trim().isEmpty()) time = LocalTime.parse(timePicker.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                task.setDeadline(LocalDateTime.of(datePicker.getValue(), time));
            } catch (Exception ex) { task.setDeadline(LocalDateTime.of(datePicker.getValue(), LocalTime.MIDNIGHT)); }
        } else {
            task.setDeadline(null);
        }
    }

    private MenuButton buildDependenciesMenu(TaskItem task, SectionConfig config, List<TaskItem> db) {
        MenuButton menu = new MenuButton("Dependencies (0)");
        menu.getStyleClass().add("custom-menu-btn");
        menu.setMaxWidth(Double.MAX_VALUE);
        int depCount = 0;

        for (TaskItem other : db) {
            if (other.getId().equals(task.getId()) || other.isFinished() || other.isArchived()) continue;
            if (config != null && config.getId().equals(other.getSectionId())) {
                CheckBox cb = new CheckBox(other.getTextContent());
                cb.setStyle("-fx-text-fill: white;");
                cb.setSelected(selectedDeps.contains(other.getId()));
                if (cb.isSelected()) depCount++;
                cb.setOnAction(e -> {
                    if (cb.isSelected() && !selectedDeps.contains(other.getId())) selectedDeps.add(other.getId());
                    else if (!cb.isSelected()) selectedDeps.remove(other.getId());
                    menu.setText("Dependencies (" + selectedDeps.size() + ")");
                });
                CustomMenuItem item = new CustomMenuItem(cb);
                item.setHideOnClick(false);
                menu.getItems().add(item);
            }
        }
        menu.setText("Dependencies (" + depCount + ")");
        if (menu.getItems().isEmpty()) menu.getItems().add(new CustomMenuItem(new Label("No other active tasks")) {{ setDisable(true); }});
        return menu;
    }
}