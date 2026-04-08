package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.DailyTemplate;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TemplateRow extends HBox {

    public TemplateRow(DailyTemplate t, int index, List<DailyTemplate> templates, SectionConfig selected, AppStats appStats, Runnable onUpdate) {
        super(10);
        setAlignment(Pos.CENTER_LEFT);

        String bgColor = (t.getBgColor() != null && !t.getBgColor().equals("transparent")) ? t.getBgColor() : "#2D2D30";
        if (t.isOptional()) bgColor = "#332B00";
        setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 10; -fx-border-color: " + (t.isOptional() ? "#FFD700" : "#3E3E42") + "; -fx-border-radius: 5;");

        HBox textContainer = new HBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        if (t.isOptional()) {
            Label optLbl = new Label("[OPT]");
            optLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-padding: 0 5 0 0;");
            textContainer.getChildren().add(optLbl);
        }

        if (selected.isEnableIcons() && t.getIconSymbol() != null && !t.getIconSymbol().equals("None")) {
            Label iconLbl = new Label(t.getIconSymbol());
            iconLbl.setStyle("-fx-text-fill: " + (t.getIconColor() != null ? t.getIconColor() : "#FFFFFF") + "; -fx-font-size: 16px;");
            textContainer.getChildren().add(iconLbl);
        }
        if (selected.isShowPrefix() && t.getPrefix() != null && !t.getPrefix().isEmpty()) {
            Label prefLbl = new Label("[" + t.getPrefix() + "]");
            prefLbl.setStyle("-fx-text-fill: " + (t.getPrefixColor() != null ? t.getPrefixColor() : "#4EC9B0") + "; -fx-font-weight: bold;");
            textContainer.getChildren().add(prefLbl);
        }

        Label textLbl = new Label(t.getText());
        textLbl.setStyle("-fx-text-fill: white;");
        textContainer.getChildren().add(textLbl);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        StringBuilder daysStr = new StringBuilder("Days: ");
        if (t.getActiveDays().size() == 7) {
            daysStr.append("Everyday");
        } else {
            DayOfWeek[] order = {DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
            String[] letters = {"S", "M", "T", "W", "T", "F", "S"};
            for (int j = 0; j < order.length; j++) {
                if (t.getActiveDays().contains(order[j])) daysStr.append(letters[j]).append(" ");
            }
        }

        Label daysLbl = new Label(daysStr.toString().trim());
        daysLbl.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");

        Button upBtn = new Button("▲");
        upBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 8;");
        upBtn.setDisable(index == 0);
        upBtn.setOnAction(e -> { Collections.swap(templates, index, index - 1); onUpdate.run(); });

        Button downBtn = new Button("▼");
        downBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 8;");
        downBtn.setDisable(index == templates.size() - 1);
        downBtn.setOnAction(e -> { Collections.swap(templates, index, index + 1); onUpdate.run(); });

        Button copyBtn = new Button("📋");
        copyBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 8; -fx-font-size: 12px;");
        copyBtn.setOnAction(e -> {
            DailyTemplate clone = new DailyTemplate(t.getPrefix(), t.getText() + " (Copy)", t.getPrefixColor(), t.getBgColor());
            clone.setActiveDays(new ArrayList<>(t.getActiveDays()));
            clone.setIconSymbol(t.getIconSymbol()); clone.setIconColor(t.getIconColor());
            clone.setPriorityName(t.getPriorityName()); clone.setTaskType(t.getTaskType());
            clone.setRewardPoints(t.getRewardPoints()); clone.setPenaltyPoints(t.getPenaltyPoints());
            clone.setOptional(t.isOptional());
            if (t.getSubTaskLines() != null) clone.setSubTaskLines(new ArrayList<>(t.getSubTaskLines()));

            // --- FIXED: Ensure NEW properties duplicate perfectly ---
            clone.setCustomOutlineColor(t.getCustomOutlineColor());
            clone.setCustomSideboxColor(t.getCustomSideboxColor());
            clone.setRepeatingMode(t.isRepeatingMode());
            clone.setRepetitionCount(t.getRepetitionCount());

            if (t.getStatRewards() != null) clone.setStatRewards(new HashMap<>(t.getStatRewards()));
            if (t.getStatCapRewards() != null) clone.setStatCapRewards(new HashMap<>(t.getStatCapRewards()));
            if (t.getStatCosts() != null) clone.setStatCosts(new HashMap<>(t.getStatCosts()));
            if (t.getStatPenalties() != null) clone.setStatPenalties(new HashMap<>(t.getStatPenalties()));
            if (t.getStatRequirements() != null) clone.setStatRequirements(new HashMap<>(t.getStatRequirements()));

            templates.add(index + 1, clone);
            onUpdate.run();
        });

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
        editBtn.setOnAction(e -> TemplateEditDialog.show(t, selected, appStats, onUpdate));

        Button delBtn = new Button("Delete");
        delBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand;");
        delBtn.setOnAction(e -> { selected.getAutoAddTemplates().remove(t); onUpdate.run(); });

        getChildren().addAll(textContainer, daysLbl, upBtn, downBtn, copyBtn, editBtn, delBtn);
    }
}