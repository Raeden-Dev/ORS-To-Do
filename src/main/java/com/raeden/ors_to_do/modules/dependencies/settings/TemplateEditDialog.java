package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.DailyTemplate;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateEditDialog {

    public static void show(DailyTemplate template, SectionConfig section, AppStats appStats, Runnable onSave) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(template == null ? "Add Template" : "Edit Template");
        TaskDialogs.styleDialog(dialog);

        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(10));

        // ==========================================
        // 1. UNIFIED FORM GRID
        // ==========================================
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        // Lock the Label column to an exact width for perfect alignment
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(135);
        col1.setPrefWidth(135);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        int r = 0;

        // Exact pixel widths to align perfectly to the right edge
        double FULL_WIDTH = 420.0;
        double HALF_WIDTH = 205.0; // 205 + 10px gap + 205 = 420px

        // --- Task Text ---
        TextField textField = new TextField(template != null ? template.getText() : "");
        textField.setPrefWidth(FULL_WIDTH);
        textField.setMaxWidth(FULL_WIDTH);
        grid.add(new Label("Task Text:"), 0, r);
        grid.add(textField, 1, r++);

        // --- Active Days ---
        Label daysLabel = new Label("Active Days:");
        daysLabel.setStyle("-fx-text-fill: white;");
        HBox daysBox = new HBox(8);
        List<CheckBox> dayChecks = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            CheckBox cb = new CheckBox(day.name().substring(0, 3));
            cb.setStyle("-fx-text-fill: white;");
            if (template == null || template.getActiveDays().contains(day)) cb.setSelected(true);
            cb.setUserData(day); dayChecks.add(cb); daysBox.getChildren().add(cb);
        }
        grid.add(daysLabel, 0, r);
        grid.add(daysBox, 1, r++);

        // --- Optional Toggle ---
        CheckBox optionalCheck = new CheckBox("Is Optional Task?");
        optionalCheck.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
        optionalCheck.setSelected(template != null && template.isOptional());
        if (!section.isEnableOptionalTasks()) {
            optionalCheck.setDisable(true); optionalCheck.setSelected(false);
            optionalCheck.setText("Is Optional Task? (Disabled)");
        }
        grid.add(optionalCheck, 1, r++);

        // --- Separator (Replaces Visual Customization Header) ---
        Separator visualSep = new Separator();
        GridPane.setMargin(visualSep, new Insets(5, 0, 5, 0));
        grid.add(visualSep, 0, r++, 2, 1);

        // --- Background Color ---
        ColorPicker bgColorPicker = new ColorPicker();
        bgColorPicker.setValue(template != null && template.getBgColor() != null && !template.getBgColor().equals("transparent") ? Color.web(template.getBgColor()) : Color.TRANSPARENT);
        bgColorPicker.setPrefWidth(FULL_WIDTH); bgColorPicker.setMaxWidth(FULL_WIDTH);
        grid.add(new Label("Background Color:"), 0, r);
        grid.add(bgColorPicker, 1, r++);

        // --- Outline Color ---
        ColorPicker outlineColorPicker = new ColorPicker();
        outlineColorPicker.setValue(template != null && template.getCustomOutlineColor() != null && !template.getCustomOutlineColor().equals("transparent") ? Color.web(template.getCustomOutlineColor()) : Color.TRANSPARENT);
        outlineColorPicker.setPrefWidth(FULL_WIDTH); outlineColorPicker.setMaxWidth(FULL_WIDTH);
        grid.add(new Label("Outline Color:"), 0, r);
        grid.add(outlineColorPicker, 1, r++);

        // --- Sidebox Color ---
        ColorPicker sideboxColorPicker = new ColorPicker();
        sideboxColorPicker.setValue(template != null && template.getCustomSideboxColor() != null && !template.getCustomSideboxColor().equals("transparent") ? Color.web(template.getCustomSideboxColor()) : Color.TRANSPARENT);
        sideboxColorPicker.setPrefWidth(FULL_WIDTH); sideboxColorPicker.setMaxWidth(FULL_WIDTH);
        grid.add(new Label("Sidebox Color:"), 0, r);
        grid.add(sideboxColorPicker, 1, r++);

        // --- Prefix ---
        TextField prefixField = new TextField();
        ColorPicker prefixColor = new ColorPicker(Color.web("#4EC9B0"));
        if (section.isShowPrefix()) {
            prefixField.setText(template != null && template.getPrefix() != null ? template.getPrefix() : "");
            prefixColor.setValue(Color.web(template != null && template.getPrefixColor() != null ? template.getPrefixColor() : "#4EC9B0"));

            prefixField.setPrefWidth(HALF_WIDTH); prefixField.setMaxWidth(HALF_WIDTH);
            prefixColor.setPrefWidth(HALF_WIDTH); prefixColor.setMaxWidth(HALF_WIDTH);

            HBox prefixHBox = new HBox(10, prefixField, prefixColor);
            grid.add(new Label("Prefix (Optional):"), 0, r);
            grid.add(prefixHBox, 1, r++);
        }

        // --- Icon ---
        ComboBox<String> iconBox = new ComboBox<>();
        ColorPicker iconColorPicker = new ColorPicker(Color.WHITE);
        if (section.isEnableIcons()) {
            iconBox.getItems().addAll(TaskDialogs.ICON_LIST);
            iconBox.setValue(template != null && template.getIconSymbol() != null ? template.getIconSymbol() : "None");
            iconColorPicker.setValue(Color.web(template != null && template.getIconColor() != null ? template.getIconColor() : "#FFFFFF"));

            iconBox.setPrefWidth(HALF_WIDTH); iconBox.setMaxWidth(HALF_WIDTH);
            iconColorPicker.setPrefWidth(HALF_WIDTH); iconColorPicker.setMaxWidth(HALF_WIDTH);

            HBox iconHBox = new HBox(10, iconBox, iconColorPicker);
            grid.add(new Label("Task Icon:"), 0, r);
            grid.add(iconHBox, 1, r++);
        }

        // --- Randomize Style ---
        Button randomBtn = new Button("🎲 Randomize Style");
        randomBtn.setPrefWidth(FULL_WIDTH); randomBtn.setMaxWidth(FULL_WIDTH);
        randomBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
        randomBtn.setOnAction(e -> {
            java.util.Random rand = new java.util.Random();
            double hue = rand.nextDouble() * 360.0;
            if (section.isEnableIcons()) iconBox.setValue(TaskDialogs.ICON_LIST[rand.nextInt(TaskDialogs.ICON_LIST.length - 1) + 1]);
            iconColorPicker.setValue(Color.hsb(hue, 0.5, 0.95));
            bgColorPicker.setValue(Color.hsb(hue, 0.8, 0.2));
            outlineColorPicker.setValue(Color.hsb(hue, 0.8, 0.8));
            sideboxColorPicker.setValue(Color.hsb(hue, 0.8, 0.8));
            if (section.isShowPrefix()) prefixColor.setValue(Color.hsb(hue, 0.8, 0.8));
        });
        grid.add(randomBtn, 1, r++);

        // --- Priority ---
        ComboBox<CustomPriority> prioBox = new ComboBox<>();
        if (section.isShowPriority()) {
            prioBox.getItems().addAll(appStats.getCustomPriorities());
            TaskDialogs.setupPriorityBoxColors(prioBox);
            if (template != null && template.getPriorityName() != null) {
                appStats.getCustomPriorities().stream().filter(p -> p.getName().equals(template.getPriorityName())).findFirst().ifPresent(prioBox::setValue);
            }
            prioBox.setPrefWidth(FULL_WIDTH); prioBox.setMaxWidth(FULL_WIDTH);
            grid.add(new Label("Default Priority:"), 0, r);
            grid.add(prioBox, 1, r++);
        }

        optionalCheck.setOnAction(e -> {
            if (section.isShowPriority()) {
                prioBox.setDisable(optionalCheck.isSelected());
                if (optionalCheck.isSelected()) prioBox.setValue(null);
            }
        });
        if (template != null && template.isOptional() && section.isShowPriority()) { prioBox.setDisable(true); prioBox.setValue(null); }

        // --- Task Type ---
        TextField workTypeField = new TextField();
        if (section.isShowTaskType()) {
            workTypeField.setText(template != null && template.getTaskType() != null ? template.getTaskType() : "");
            workTypeField.setPrefWidth(FULL_WIDTH); workTypeField.setMaxWidth(FULL_WIDTH);
            grid.add(new Label("Task Type:"), 0, r);
            grid.add(workTypeField, 1, r++);
        }

        // --- Reward / Penalty Points ---
        TextField rewardField = new TextField("0");
        TextField penaltyField = new TextField("0");
        if (section.isEnableScore()) {
            rewardField.setText(template != null ? String.valueOf(template.getRewardPoints()) : "0");
            penaltyField.setText(template != null ? String.valueOf(template.getPenaltyPoints()) : "0");

            // Exact calculation: 140 + 10 (gap) + 120 (label) + 10 (gap) + 140 = 420px
            rewardField.setPrefWidth(140); rewardField.setMaxWidth(140);
            penaltyField.setPrefWidth(140); penaltyField.setMaxWidth(140);

            Label penLabel = new Label("Penalty Points:");
            penLabel.setStyle("-fx-text-fill: #E0E0E0;");
            penLabel.setPrefWidth(120);
            penLabel.setAlignment(Pos.CENTER_RIGHT);

            HBox ptsBox = new HBox(10, rewardField, penLabel, penaltyField);
            ptsBox.setAlignment(Pos.CENTER_LEFT);

            grid.add(new Label("Reward Points:"), 0, r);
            grid.add(ptsBox, 1, r++);
        }

        // --- Sub-Tasks ---
        TextArea subTasksArea = new TextArea();
        if (section.isEnableSubTasks()) {
            subTasksArea.setPromptText("Enter sub-tasks...\nOne sub-task per line");
            subTasksArea.setPrefRowCount(3);
            subTasksArea.setStyle("-fx-control-inner-background: #2D2D30; -fx-text-fill: white;");
            if (template != null && template.getSubTaskLines() != null) subTasksArea.setText(String.join("\n", template.getSubTaskLines()));

            subTasksArea.setPrefWidth(FULL_WIDTH); subTasksArea.setMaxWidth(FULL_WIDTH);
            grid.add(new Label("Sub-Tasks:"), 0, r);
            grid.add(subTasksArea, 1, r++);
        }

        mainContent.getChildren().add(grid);

        // ==========================================
        // 2. RPG STATS GRID
        // ==========================================
        Map<String, TextField> statRewardFields = new HashMap<>();
        Map<String, TextField> statCapFields = new HashMap<>();
        Map<String, TextField> statCostFields = new HashMap<>();
        Map<String, TextField> statPenFields = new HashMap<>();

        if (section.isEnableStatsSystem()) {
            mainContent.getChildren().add(new Separator());
            Label rpgHeader = new Label("RPG Stat Modifiers:");
            rpgHeader.setStyle("-fx-text-fill: #B5CEA8; -fx-font-weight: bold;");
            mainContent.getChildren().add(rpgHeader);

            GridPane rpgGrid = new GridPane();
            rpgGrid.setHgap(10); rpgGrid.setVgap(10);

            // Colored Headers
            Label lblRew = new Label("+ Reward"); lblRew.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");
            Label lblCap = new Label("+ Max Cap"); lblCap.setStyle("-fx-text-fill: #C586C0; -fx-font-weight: bold;");
            Label lblCost = new Label("- Cost"); lblCost.setStyle("-fx-text-fill: #FF8C00; -fx-font-weight: bold;");
            Label lblPen = new Label("- Penalty"); lblPen.setStyle("-fx-text-fill: #FF6666; -fx-font-weight: bold;");

            rpgGrid.add(lblRew, 1, 0);
            rpgGrid.add(lblCap, 2, 0);
            rpgGrid.add(lblCost, 3, 0);
            rpgGrid.add(lblPen, 4, 0);

            int rr = 1;
            for (CustomStat stat : appStats.getCustomStats()) {
                Label sLabel = new Label(stat.getName() + ":");
                sLabel.setStyle("-fx-text-fill: " + (stat.getTextColor() != null ? stat.getTextColor() : "white") + ";");
                rpgGrid.add(sLabel, 0, rr);

                String sid = stat.getId();

                TextField rewF = new TextField(); rewF.setPrefWidth(60);
                if (template != null && template.getStatRewards() != null && template.getStatRewards().containsKey(sid)) rewF.setText(String.valueOf(template.getStatRewards().get(sid)));
                statRewardFields.put(sid, rewF); rpgGrid.add(rewF, 1, rr);

                TextField capF = new TextField(); capF.setPrefWidth(60);
                if (template != null && template.getStatCapRewards() != null && template.getStatCapRewards().containsKey(sid)) capF.setText(String.valueOf(template.getStatCapRewards().get(sid)));
                statCapFields.put(sid, capF); rpgGrid.add(capF, 2, rr);

                TextField costF = new TextField(); costF.setPrefWidth(60);
                if (template != null && template.getStatCosts() != null && template.getStatCosts().containsKey(sid)) costF.setText(String.valueOf(template.getStatCosts().get(sid)));
                statCostFields.put(sid, costF); rpgGrid.add(costF, 3, rr);

                TextField penF = new TextField(); penF.setPrefWidth(60);
                if (template != null && template.getStatPenalties() != null && template.getStatPenalties().containsKey(sid)) penF.setText(String.valueOf(template.getStatPenalties().get(sid)));
                statPenFields.put(sid, penF); rpgGrid.add(penF, 4, rr);

                rr++;
            }
            mainContent.getChildren().add(rpgGrid);
        }

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(600, 650);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);

        String scrollCss = ".scroll-bar:vertical, .scroll-bar:horizontal { -fx-background-color: transparent; } " +
                ".scroll-bar:vertical .track, .scroll-bar:horizontal .track { -fx-background-color: #1E1E1E; -fx-border-color: transparent; } " +
                ".scroll-bar:vertical .thumb, .scroll-bar:horizontal .thumb { -fx-background-color: #555555; -fx-background-radius: 5; }";
        scrollPane.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(scrollCss.getBytes()));

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !textField.getText().trim().isEmpty()) {
                List<DayOfWeek> selectedDays = new ArrayList<>();
                for (CheckBox cb : dayChecks) if (cb.isSelected()) selectedDays.add((DayOfWeek) cb.getUserData());

                DailyTemplate tToSave = template != null ? template : new DailyTemplate(null, "", null, null);
                tToSave.setText(textField.getText().trim());
                tToSave.setActiveDays(selectedDays);

                tToSave.setBgColor(toHexString(bgColorPicker.getValue()));
                tToSave.setCustomOutlineColor(toHexString(outlineColorPicker.getValue()));
                tToSave.setCustomSideboxColor(toHexString(sideboxColorPicker.getValue()));

                tToSave.setRepeatingMode(false);
                tToSave.setRepetitionCount(0);

                tToSave.setOptional(optionalCheck.isSelected());

                if (section.isEnableIcons()) {
                    tToSave.setIconSymbol(iconBox.getValue());
                    tToSave.setIconColor(toHexString(iconColorPicker.getValue()));
                }
                if (section.isShowPrefix()) {
                    tToSave.setPrefix(prefixField.getText().trim());
                    tToSave.setPrefixColor(toHexString(prefixColor.getValue()));
                }
                if (section.isShowPriority() && prioBox.getValue() != null && !tToSave.isOptional()) {
                    tToSave.setPriorityName(prioBox.getValue().getName());
                } else if (tToSave.isOptional()) tToSave.setPriorityName(null);

                if (section.isShowTaskType()) tToSave.setTaskType(workTypeField.getText().trim());

                if (section.isEnableScore()) {
                    try { tToSave.setRewardPoints(Integer.parseInt(rewardField.getText().trim())); } catch(Exception ignore){}
                    try { tToSave.setPenaltyPoints(Integer.parseInt(penaltyField.getText().trim())); } catch(Exception ignore){}
                }

                if (section.isEnableSubTasks()) {
                    List<String> lines = new ArrayList<>();
                    for(String line : subTasksArea.getText().split("\n")) if (!line.trim().isEmpty()) lines.add(line.trim());
                    tToSave.setSubTaskLines(lines);
                }

                if (section.isEnableStatsSystem()) {
                    Map<String, Integer> nRewards = new HashMap<>(); Map<String, Integer> nCaps = new HashMap<>();
                    Map<String, Integer> nCosts = new HashMap<>(); Map<String, Integer> nPens = new HashMap<>();

                    for (CustomStat stat : appStats.getCustomStats()) {
                        String sid = stat.getId();
                        try { int v = Integer.parseInt(statRewardFields.get(sid).getText().trim()); if(v>0) nRewards.put(sid, v); } catch(Exception ignore){}
                        try { int v = Integer.parseInt(statCapFields.get(sid).getText().trim());    if(v>0) nCaps.put(sid, v); } catch(Exception ignore){}
                        try { int v = Integer.parseInt(statCostFields.get(sid).getText().trim());   if(v>0) nCosts.put(sid, v); } catch(Exception ignore){}
                        try { int v = Integer.parseInt(statPenFields.get(sid).getText().trim());    if(v>0) nPens.put(sid, v); } catch(Exception ignore){}
                    }
                    tToSave.setStatRewards(nRewards); tToSave.setStatCapRewards(nCaps);
                    tToSave.setStatCosts(nCosts); tToSave.setStatPenalties(nPens);

                    // Preserve old requirements invisibly if they existed
                    if (template != null && template.getStatRequirements() != null) {
                        tToSave.setStatRequirements(new HashMap<>(template.getStatRequirements()));
                    }
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