package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.*;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs.*;

public class TaskEditDialog {

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

        TextArea contentField = new TextArea(task.getTextContent() != null ? task.getTextContent() : "");
        contentField.setMaxWidth(Double.MAX_VALUE);
        contentField.setWrapText(true);
        contentField.setPrefRowCount(config.isNotesPage() ? 6 : 2);

        grid.add(new Label(config.isNotesPage() ? "Note Text:" : (config.isRewardsPage() ? "Reward Name:" : "Content:")), 0, rowIdx);
        grid.add(contentField, 1, rowIdx++);

        CheckBox linkCardCheck = new CheckBox("Is Link Card?");
        linkCardCheck.setStyle("-fx-text-fill: white;");

        boolean hasSubTasks = task.getSubTasks() != null && !task.getSubTasks().isEmpty();
        boolean sectionAllowsLinks = config == null || config.isEnableLinkCards();

        if (!sectionAllowsLinks || hasSubTasks || task.isOptional()) {
            linkCardCheck.setDisable(true);
            linkCardCheck.setSelected(false);
            if (task.isOptional()) {
                linkCardCheck.setText("Is Link Card? (Disabled: Optional Task)");
            } else if (hasSubTasks) {
                linkCardCheck.setText("Is Link Card? (Disabled: Has Sub-Tasks)");
            } else {
                linkCardCheck.setText("Is Link Card? (Disabled by Section Config)");
            }
        } else {
            linkCardCheck.setSelected(task.isLinkCard());
        }

        TextField linkPathField = new TextField(task.getLinkActionPath() != null ? task.getLinkActionPath() : "");
        linkPathField.setPromptText("Enter URL, Folder path, or App path (.exe)");
        linkPathField.setMaxWidth(Double.MAX_VALUE);
        linkPathField.setDisable(!linkCardCheck.isSelected());

        linkCardCheck.setOnAction(e -> linkPathField.setDisable(!linkCardCheck.isSelected()));

        grid.add(linkCardCheck, 0, rowIdx);
        grid.add(linkPathField, 1, rowIdx++);

        boolean allowStyling = config != null && (config.isNotesPage() || config.isEnableTaskStyling());
        boolean allowIcons = config == null || config.isEnableIcons();
        boolean allowPrefix = config == null || config.isShowPrefix();

        ColorPicker bgColorPicker = null;
        ColorPicker outlinePicker = null;
        ColorPicker sideboxPicker = null;
        ComboBox<String> iconBox = null;
        ColorPicker iconColorPicker = null;
        TextField prefixFieldEdit = null;
        ColorPicker preC = null;

        if (allowStyling || allowIcons || allowPrefix) {
            grid.add(new Separator(), 0, rowIdx, 2, 1); rowIdx++;

            Label styleHeader = new Label("Task Appearance & Styling:");
            styleHeader.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold;");
            grid.add(styleHeader, 0, rowIdx, 2, 1); rowIdx++;

            if (allowStyling) {
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

            if (allowIcons) {
                iconBox = new ComboBox<>();
                iconBox.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(iconBox, Priority.ALWAYS);
                iconBox.getItems().addAll(ICON_LIST);
                iconBox.setValue(task.getIconSymbol() != null ? task.getIconSymbol() : "None");

                iconColorPicker = new ColorPicker(Color.web(task.getIconColor() != null ? task.getIconColor() : "#FFFFFF"));

                HBox iconRow = new HBox(10, iconBox, iconColorPicker);
                grid.add(new Label("Icon & Color:"), 0, rowIdx); grid.add(iconRow, 1, rowIdx++);
            }

            if (allowPrefix) {
                prefixFieldEdit = new TextField(task.getPrefix() != null ? task.getPrefix() : "");
                prefixFieldEdit.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(prefixFieldEdit, Priority.ALWAYS);

                preC = new ColorPicker(Color.web(task.getPrefixColor() != null ? task.getPrefixColor() : "#4EC9B0"));

                HBox prefixRow = new HBox(10, prefixFieldEdit, preC);
                grid.add(new Label("Prefix & Color:"), 0, rowIdx); grid.add(prefixRow, 1, rowIdx++);
            }

            Button randomBtn = new Button("🎲 Randomize Style");
            randomBtn.setMaxWidth(Double.MAX_VALUE);

            ColorPicker finalBgColorPicker = bgColorPicker;
            ColorPicker finalOutlinePicker = outlinePicker;
            ColorPicker finalSideboxPicker = sideboxPicker;
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
                if (finalBgColorPicker != null) {
                    finalBgColorPicker.setValue(Color.hsb(hue, 0.8, 0.2));
                }
                if (finalOutlinePicker != null) {
                    finalOutlinePicker.setValue(Color.hsb(hue, 0.8, 0.8));
                }
                if (finalSideboxPicker != null) {
                    finalSideboxPicker.setValue(Color.hsb(hue, 0.6, 0.9));
                }
            });
            grid.add(randomBtn, 1, rowIdx++);

            grid.add(new Separator(), 0, rowIdx, 2, 1); rowIdx++;
        }

        MenuButton dependenciesMenu = new MenuButton("Dependencies (0)");
        dependenciesMenu.getStyleClass().add("custom-menu-btn");
        dependenciesMenu.setMaxWidth(Double.MAX_VALUE);
        List<String> selectedDeps = new ArrayList<>(task.getDependsOnTaskIds());
        int depCount = 0;

        for (TaskItem other : globalDatabase) {
            if (other.getId().equals(task.getId())) continue;
            if (other.isFinished() || other.isArchived()) continue;
            if (config != null && config.getId().equals(other.getSectionId())) {
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

        if (config == null || !config.isNotesPage()) {
            grid.add(new Label((config != null && config.isRewardsPage()) ? "Unlock Condition:" : "Depends On:"), 0, rowIdx); grid.add(dependenciesMenu, 1, rowIdx++);
        }

        ComboBox<CustomPriority> prioBoxEdit = null;
        if ((config == null || (config.isShowPriority() && !config.isNotesPage())) && !task.isOptional()) {
            prioBoxEdit = new ComboBox<>();
            prioBoxEdit.setMaxWidth(Double.MAX_VALUE);
            prioBoxEdit.getItems().addAll(appStats.getCustomPriorities());
            prioBoxEdit.setValue(task.getPriority());
            setupPriorityBoxColors(prioBoxEdit);
            grid.add(new Label((config != null && config.isRewardsPage()) ? "Reward Tier:" : "Priority:"), 0, rowIdx); grid.add(prioBoxEdit, 1, rowIdx++);
        }

        TextField taskTypeField = null;
        if (config == null || config.isShowTaskType()) {
            taskTypeField = new TextField(task.getTaskType() != null ? task.getTaskType() : "");
            taskTypeField.setMaxWidth(Double.MAX_VALUE);
            grid.add(new Label("Category:"), 0, rowIdx); grid.add(taskTypeField, 1, rowIdx++);
        }

        DatePicker datePicker = new DatePicker();
        TextField timePicker = new TextField();
        TextField maxCountField = new TextField(String.valueOf(task.getMaxCount()));
        TextField costField = new TextField(String.valueOf(task.getCostPoints()));
        TextField rewardField = new TextField(String.valueOf(task.getRewardPoints()));
        TextField penaltyField = new TextField(String.valueOf(task.getPenaltyPoints()));

        Map<String, TextField> statRewardFields = new HashMap<>();
        Map<String, TextField> statPenaltyFields = new HashMap<>();

        if (config == null || !config.isNotesPage()) {
            datePicker.setMaxWidth(Double.MAX_VALUE);
            if (task.getDeadline() != null) datePicker.setValue(task.getDeadline().toLocalDate());

            timePicker.setMaxWidth(Double.MAX_VALUE);
            timePicker.setPromptText("HH:mm (24h)");
            if (task.getDeadline() != null) timePicker.setText(task.getDeadline().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

            // --- FIXED: Disable Exact Time box if no deadline date is selected ---
            timePicker.setDisable(datePicker.getValue() == null);
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> timePicker.setDisable(newVal == null));

            grid.add(new Label((config != null && config.isRewardsPage()) ? "Available Until:" : "Deadline Date:"), 0, rowIdx); grid.add(datePicker, 1, rowIdx++);
            grid.add(new Label("Exact Time:"), 0, rowIdx); grid.add(timePicker, 1, rowIdx++);

            grid.add(new Separator(), 0, rowIdx, 2, 1); rowIdx++;

            maxCountField.setMaxWidth(Double.MAX_VALUE);
            maxCountField.setPromptText((config != null && config.isRewardsPage()) ? "0 = Infinite purchases" : "0 = Standard task (No counter)");
            grid.add(new Label((config != null && config.isRewardsPage()) ? "Max Purchases:" : "Counter Goal (0=Off):"), 0, rowIdx); grid.add(maxCountField, 1, rowIdx++);

            costField.setMaxWidth(Double.MAX_VALUE);
            rewardField.setMaxWidth(Double.MAX_VALUE);
            penaltyField.setMaxWidth(Double.MAX_VALUE);

            if (config != null && config.isRewardsPage()) {
                grid.add(new Label("Reward Cost (Points):"), 0, rowIdx); grid.add(costField, 1, rowIdx++);
            } else if (config == null || config.isEnableScore()) {
                grid.add(new Label("Reward Points:"), 0, rowIdx); grid.add(rewardField, 1, rowIdx++);
                grid.add(new Label("Missed Penalty:"), 0, rowIdx); grid.add(penaltyField, 1, rowIdx++);
            }

            if ((config == null || config.isEnableStatsSystem()) && !appStats.getCustomStats().isEmpty()) {
                grid.add(new Separator(), 0, rowIdx, 2, 1); rowIdx++;

                Label statHeader = new Label("Custom Stat Rewards & Penalties:");
                statHeader.setStyle("-fx-text-fill: #B5CEA8; -fx-font-weight: bold;");
                grid.add(statHeader, 0, rowIdx, 2, 1); rowIdx++;

                for (CustomStat stat : appStats.getCustomStats()) {
                    String icon = stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") ? stat.getIconSymbol() + " " : "";
                    Label statLabel = new Label(icon + stat.getName() + ":");
                    statLabel.setStyle("-fx-text-fill: " + (stat.getTextColor() != null ? stat.getTextColor() : "#FFFFFF") + ";");

                    int currentReward = task.getStatRewards().getOrDefault(stat.getId(), 0);
                    int currentPenalty = task.getStatPenalties().getOrDefault(stat.getId(), 0);

                    TextField sRewardField = new TextField(currentReward > 0 ? String.valueOf(currentReward) : "");
                    sRewardField.setPromptText("+XP");
                    sRewardField.setPrefWidth(60);

                    TextField sPenaltyField = new TextField(currentPenalty > 0 ? String.valueOf(currentPenalty) : "");
                    sPenaltyField.setPromptText("-XP");
                    sPenaltyField.setPrefWidth(60);

                    HBox statBox = new HBox(10, new Label("+"), sRewardField, new Label("-"), sPenaltyField);
                    statBox.setAlignment(Pos.CENTER_LEFT);

                    grid.add(statLabel, 0, rowIdx);
                    grid.add(statBox, 1, rowIdx++);

                    statRewardFields.put(stat.getId(), sRewardField);
                    statPenaltyFields.put(stat.getId(), sPenaltyField);
                }
            }
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ColorPicker okBgColorPicker = bgColorPicker;
        ColorPicker okOutlinePicker = outlinePicker;
        ColorPicker okSideboxPicker = sideboxPicker;
        TextField okPrefixFieldEdit = prefixFieldEdit;
        ColorPicker okPreC = preC;
        ComboBox<String> okIconBox = iconBox;
        ColorPicker okIconColorPicker = iconColorPicker;
        ComboBox<CustomPriority> okPrioBoxEdit = prioBoxEdit;
        TextField okTaskTypeField = taskTypeField;

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String typedContent = contentField.getText() != null ? contentField.getText().trim() : "";
                if (typedContent.isEmpty() && linkCardCheck.isSelected() && linkPathField.getText() != null && !linkPathField.getText().trim().isEmpty()) {
                    typedContent = linkPathField.getText().trim();
                }

                task.setTextContent(typedContent);

                if (linkCardCheck.isSelected()) {
                    task.setLinkCard(true);
                    task.setLinkActionPath(linkPathField.getText() != null ? linkPathField.getText().trim() : "");
                } else {
                    task.setLinkCard(false);
                    task.setLinkActionPath("");
                }

                if (allowStyling) {
                    if (okBgColorPicker != null) task.setColorHex(toHexString(okBgColorPicker.getValue()));
                    if (okOutlinePicker != null) task.setCustomOutlineColor(toHexString(okOutlinePicker.getValue()));
                    if (okSideboxPicker != null) task.setCustomSideboxColor(toHexString(okSideboxPicker.getValue()));
                }

                if (config == null || !config.isNotesPage()) {
                    task.setDependsOnTaskIds(selectedDeps);

                    if (okPrioBoxEdit != null) {
                        task.setPriority(okPrioBoxEdit.getValue());
                    } else if (task.isOptional()) {
                        task.setPriority(null);
                    }

                    if (datePicker.getValue() != null) {
                        try {
                            LocalTime time = LocalTime.MIDNIGHT;
                            if (!timePicker.getText().trim().isEmpty()) time = LocalTime.parse(timePicker.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                            task.setDeadline(LocalDateTime.of(datePicker.getValue(), time));
                        } catch (Exception ex) { task.setDeadline(LocalDateTime.of(datePicker.getValue(), LocalTime.MIDNIGHT)); }
                    } else task.setDeadline(null);

                    int maxC = 0;
                    try { maxC = Math.max(0, Integer.parseInt(maxCountField.getText() != null && !maxCountField.getText().isEmpty() ? maxCountField.getText().trim() : "0")); } catch (Exception ignore) {}
                    task.setMaxCount(maxC);
                    task.setCounterMode(maxC > 0);

                    if (config != null && config.isRewardsPage()) {
                        try { task.setCostPoints(Math.max(0, Integer.parseInt(costField.getText() != null && !costField.getText().isEmpty() ? costField.getText().trim() : "0"))); } catch (Exception ignore) {}
                    } else if (config == null || config.isEnableScore()) {
                        try { task.setRewardPoints(Math.max(0, Integer.parseInt(rewardField.getText() != null && !rewardField.getText().isEmpty() ? rewardField.getText().trim() : "0"))); } catch (Exception ignore) {}
                        try { task.setPenaltyPoints(Math.max(0, Integer.parseInt(penaltyField.getText() != null && !penaltyField.getText().isEmpty() ? penaltyField.getText().trim() : "0"))); } catch (Exception ignore) {}
                    }

                    if (config == null || config.isEnableStatsSystem()) {
                        task.getStatRewards().clear();
                        task.getStatPenalties().clear();

                        for (CustomStat stat : appStats.getCustomStats()) {
                            TextField rewField = statRewardFields.get(stat.getId());
                            TextField penField = statPenaltyFields.get(stat.getId());

                            if (rewField != null && rewField.getText() != null && !rewField.getText().trim().isEmpty()) {
                                try {
                                    int rew = Integer.parseInt(rewField.getText().trim());
                                    if (rew > 0) task.getStatRewards().put(stat.getId(), rew);
                                } catch (Exception ignore) {}
                            }
                            if (penField != null && penField.getText() != null && !penField.getText().trim().isEmpty()) {
                                try {
                                    int pen = Integer.parseInt(penField.getText().trim());
                                    if (pen > 0) task.getStatPenalties().put(stat.getId(), pen);
                                } catch (Exception ignore) {}
                            }
                        }
                    }
                }

                if ((config == null || config.isEnableIcons()) && okIconBox != null) {
                    task.setIconSymbol(okIconBox.getValue());
                    task.setIconColor(toHexString(okIconColorPicker.getValue()));
                }

                if ((config == null || config.isShowPrefix()) && okPrefixFieldEdit != null) {
                    task.setPrefix(okPrefixFieldEdit.getText() != null ? okPrefixFieldEdit.getText().trim() : "");
                    task.setPrefixColor(toHexString(okPreC.getValue()));
                }
                if ((config == null || config.isShowTaskType()) && okTaskTypeField != null) {
                    task.setTaskType(okTaskTypeField.getText() != null ? okTaskTypeField.getText().trim() : "");
                }

                StorageManager.saveTasks(globalDatabase); onUpdate.run();
            }
        });
    }
}