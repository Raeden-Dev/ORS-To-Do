package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.DailyTemplate;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class TemplateEditDialog {

    public static void show(DailyTemplate template, SectionConfig section, AppStats appStats, Runnable onSave) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(template == null ? "Add Template" : "Edit Template");
        TaskDialogs.styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        int rowIdx = 0;

        TextField textField = new TextField(template != null ? template.getText() : "");
        grid.add(new Label("Task Text:"), 0, rowIdx); grid.add(textField, 1, rowIdx++);

        ColorPicker bgColorPicker = new ColorPicker();
        bgColorPicker.setValue(template != null && template.getBgColor() != null && !template.getBgColor().equals("transparent") ? Color.web(template.getBgColor()) : Color.TRANSPARENT);
        grid.add(new Label("Background Color:"), 0, rowIdx); grid.add(bgColorPicker, 1, rowIdx++);

        CheckBox optionalCheck = new CheckBox("Is Optional Task?");
        optionalCheck.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
        optionalCheck.setSelected(template != null && template.isOptional());
        if (!section.isEnableOptionalTasks()) {
            optionalCheck.setDisable(true); optionalCheck.setSelected(false);
            optionalCheck.setText("Is Optional Task? (Disabled in Section Config)");
        }
        grid.add(optionalCheck, 1, rowIdx++);

        ComboBox<String> iconBox = null; ColorPicker iconColorPicker = null;
        if (section.isEnableIcons()) {
            iconBox = new ComboBox<>(); iconBox.getItems().addAll(TaskDialogs.ICON_LIST);
            iconBox.setValue(template != null && template.getIconSymbol() != null ? template.getIconSymbol() : "None");
            iconColorPicker = new ColorPicker(Color.web(template != null && template.getIconColor() != null ? template.getIconColor() : "#FFFFFF"));
            grid.add(new Label("Task Icon:"), 0, rowIdx); grid.add(iconBox, 1, rowIdx++);
            grid.add(new Label("Icon Color:"), 0, rowIdx); grid.add(iconColorPicker, 1, rowIdx++);
        }

        TextField prefixField = null; ColorPicker prefixColor = null;
        if (section.isShowPrefix()) {
            prefixField = new TextField(template != null && template.getPrefix() != null ? template.getPrefix() : "");
            prefixColor = new ColorPicker(Color.web(template != null && template.getPrefixColor() != null ? template.getPrefixColor() : "#4EC9B0"));
            grid.add(new Label("Prefix (Optional):"), 0, rowIdx); grid.add(prefixField, 1, rowIdx++);
            grid.add(new Label("Prefix Color:"), 0, rowIdx); grid.add(prefixColor, 1, rowIdx++);
        }

        ComboBox<CustomPriority> prioBox = null;
        if (section.isShowPriority()) {
            prioBox = new ComboBox<>(); prioBox.getItems().addAll(appStats.getCustomPriorities());
            TaskDialogs.setupPriorityBoxColors(prioBox);
            if (template != null && template.getPriorityName() != null) {
                appStats.getCustomPriorities().stream().filter(p -> p.getName().equals(template.getPriorityName())).findFirst().ifPresent(prioBox::setValue);
            }
            grid.add(new Label("Default Priority:"), 0, rowIdx); grid.add(prioBox, 1, rowIdx++);
        }

        ComboBox<CustomPriority> finalPrioBoxForListener = prioBox;
        optionalCheck.setOnAction(e -> {
            if (finalPrioBoxForListener != null) {
                finalPrioBoxForListener.setDisable(optionalCheck.isSelected());
                if (optionalCheck.isSelected()) finalPrioBoxForListener.setValue(null);
            }
        });
        if (template != null && template.isOptional() && prioBox != null) { prioBox.setDisable(true); prioBox.setValue(null); }

        TextField workTypeField = null;
        if (section.isShowTaskType()) {
            workTypeField = new TextField(template != null && template.getTaskType() != null ? template.getTaskType() : "");
            grid.add(new Label("Task Type:"), 0, rowIdx); grid.add(workTypeField, 1, rowIdx++);
        }

        TextField rewardField = null; TextField penaltyField = null;
        if (section.isEnableScore()) {
            rewardField = new TextField(template != null ? String.valueOf(template.getRewardPoints()) : "0");
            penaltyField = new TextField(template != null ? String.valueOf(template.getPenaltyPoints()) : "0");
            grid.add(new Label("Reward Points:"), 0, rowIdx); grid.add(rewardField, 1, rowIdx++);
            grid.add(new Label("Penalty Points:"), 0, rowIdx); grid.add(penaltyField, 1, rowIdx++);
        }

        TextArea subTasksArea = null;
        if (section.isEnableSubTasks()) {
            subTasksArea = new TextArea();
            subTasksArea.setPromptText("Enter sub-tasks...\nOne sub-task per line");
            subTasksArea.setPrefRowCount(3);
            subTasksArea.setStyle("-fx-control-inner-background: #2D2D30; -fx-text-fill: white;");
            if (template != null && template.getSubTaskLines() != null) subTasksArea.setText(String.join("\n", template.getSubTaskLines()));
            grid.add(new Label("Sub-Tasks:"), 0, rowIdx); grid.add(subTasksArea, 1, rowIdx++);
        }

        Label daysLabel = new Label("Active Days:"); daysLabel.setStyle("-fx-text-fill: white;");
        HBox daysBox = new HBox(5);
        List<CheckBox> dayChecks = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            CheckBox cb = new CheckBox(day.name().substring(0, 3));
            cb.setStyle("-fx-text-fill: white;");
            if (template == null || template.getActiveDays().contains(day)) cb.setSelected(true);
            cb.setUserData(day); dayChecks.add(cb); daysBox.getChildren().add(cb);
        }
        grid.add(daysLabel, 0, rowIdx); grid.add(daysBox, 1, rowIdx++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> finalIconBox = iconBox; ColorPicker finalIconColorPicker = iconColorPicker;
        TextField finalPrefixField = prefixField; ColorPicker finalPrefixColor = prefixColor;
        ComboBox<CustomPriority> finalPrioBox = prioBox; TextField finalWorkTypeField = workTypeField;
        TextField finalRewardField = rewardField; TextField finalPenaltyField = penaltyField; TextArea finalSubTasksArea = subTasksArea;

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !textField.getText().trim().isEmpty()) {
                List<DayOfWeek> selectedDays = new ArrayList<>();
                for (CheckBox cb : dayChecks) if (cb.isSelected()) selectedDays.add((DayOfWeek) cb.getUserData());

                DailyTemplate tToSave = template != null ? template : new DailyTemplate(null, "", null, null);
                tToSave.setText(textField.getText().trim());
                tToSave.setActiveDays(selectedDays);
                tToSave.setBgColor(toHexString(bgColorPicker.getValue()));
                tToSave.setOptional(optionalCheck.isSelected());

                if (section.isEnableIcons() && finalIconBox != null) {
                    tToSave.setIconSymbol(finalIconBox.getValue());
                    tToSave.setIconColor(toHexString(finalIconColorPicker.getValue()));
                }
                if (section.isShowPrefix() && finalPrefixField != null) {
                    tToSave.setPrefix(finalPrefixField.getText().trim());
                    tToSave.setPrefixColor(toHexString(finalPrefixColor.getValue()));
                }
                if (section.isShowPriority() && finalPrioBox != null && finalPrioBox.getValue() != null && !tToSave.isOptional()) {
                    tToSave.setPriorityName(finalPrioBox.getValue().getName());
                } else if (tToSave.isOptional()) tToSave.setPriorityName(null);

                if (section.isShowTaskType() && finalWorkTypeField != null) tToSave.setTaskType(finalWorkTypeField.getText().trim());
                if (section.isEnableScore() && finalRewardField != null) {
                    try { tToSave.setRewardPoints(Integer.parseInt(finalRewardField.getText().trim())); } catch(Exception ignore){}
                    try { tToSave.setPenaltyPoints(Integer.parseInt(finalPenaltyField.getText().trim())); } catch(Exception ignore){}
                }
                if (section.isEnableSubTasks() && finalSubTasksArea != null) {
                    List<String> lines = new ArrayList<>();
                    for(String line : finalSubTasksArea.getText().split("\n")) if (!line.trim().isEmpty()) lines.add(line.trim());
                    tToSave.setSubTaskLines(lines);
                }

                if (template == null) section.getAutoAddTemplates().add(tToSave);
                onSave.run();
            }
        });
    }

    private static String toHexString(Color color) {
        if (color == null || color.getOpacity() == 0.0) return "transparent";
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}