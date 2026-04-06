package com.raeden.ors_to_do.modules.dependencies.ui.forms;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRPGForm {
    private TextField maxCountField, costField, rewardField, penaltyField;
    private Map<String, TextField> statRewardFields = new HashMap<>();
    private Map<String, TextField> statPenaltyFields = new HashMap<>();

    public void buildUI(GridPane grid, AtomicInteger rowIdx, TaskItem task, SectionConfig config, AppStats appStats) {
        maxCountField = createNumberField(task.getMaxCount(), (config != null && config.isRewardsPage()) ? "0 = Infinite purchases" : "0 = Standard task");
        grid.add(new Label((config != null && config.isRewardsPage()) ? "Max Purchases:" : "Counter Goal (0=Off):"), 0, rowIdx.get());
        grid.add(maxCountField, 1, rowIdx.getAndIncrement());

        costField = createNumberField(task.getCostPoints(), "");
        rewardField = createNumberField(task.getRewardPoints(), "");
        penaltyField = createNumberField(task.getPenaltyPoints(), "");

        if (config != null && config.isRewardsPage()) {
            grid.add(new Label("Reward Cost (Points):"), 0, rowIdx.get()); grid.add(costField, 1, rowIdx.getAndIncrement());
        } else if (config == null || config.isEnableScore()) {
            grid.add(new Label("Reward Points:"), 0, rowIdx.get()); grid.add(rewardField, 1, rowIdx.getAndIncrement());
            grid.add(new Label("Missed Penalty:"), 0, rowIdx.get()); grid.add(penaltyField, 1, rowIdx.getAndIncrement());
        }

        if ((config == null || config.isEnableStatsSystem()) && !appStats.getCustomStats().isEmpty()) {
            grid.add(new Separator(), 0, rowIdx.get(), 2, 1); rowIdx.getAndIncrement();
            Label statHeader = new Label("Custom Stat Rewards & Penalties:");
            statHeader.setStyle("-fx-text-fill: #B5CEA8; -fx-font-weight: bold;");
            grid.add(statHeader, 0, rowIdx.get(), 2, 1); rowIdx.getAndIncrement();

            for (CustomStat stat : appStats.getCustomStats()) {
                String icon = stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") ? stat.getIconSymbol() + " " : "";
                Label statLabel = new Label(icon + stat.getName() + ":");
                statLabel.setStyle("-fx-text-fill: " + (stat.getTextColor() != null ? stat.getTextColor() : "#FFFFFF") + ";");

                int cRew = task.getStatRewards().getOrDefault(stat.getId(), 0);
                int cPen = task.getStatPenalties().getOrDefault(stat.getId(), 0);

                TextField sRewardField = createNumberField(cRew > 0 ? cRew : null, "+XP"); sRewardField.setPrefWidth(60);
                TextField sPenaltyField = createNumberField(cPen > 0 ? cPen : null, "-XP"); sPenaltyField.setPrefWidth(60);

                HBox statBox = new HBox(10, new Label("+"), sRewardField, new Label("-"), sPenaltyField);
                statBox.setAlignment(Pos.CENTER_LEFT);

                grid.add(statLabel, 0, rowIdx.get()); grid.add(statBox, 1, rowIdx.getAndIncrement());
                statRewardFields.put(stat.getId(), sRewardField);
                statPenaltyFields.put(stat.getId(), sPenaltyField);
            }
        }
    }

    public void applyTo(TaskItem task, SectionConfig config, AppStats appStats) {
        task.setMaxCount(parse(maxCountField.getText()));
        task.setCounterMode(task.getMaxCount() > 0);

        if (config != null && config.isRewardsPage()) {
            task.setCostPoints(parse(costField.getText()));
        } else if (config == null || config.isEnableScore()) {
            task.setRewardPoints(parse(rewardField.getText()));
            task.setPenaltyPoints(parse(penaltyField.getText()));
        }

        if (config == null || config.isEnableStatsSystem()) {
            task.getStatRewards().clear();
            task.getStatPenalties().clear();

            for (CustomStat stat : appStats.getCustomStats()) {
                TextField rewField = statRewardFields.get(stat.getId());
                TextField penField = statPenaltyFields.get(stat.getId());

                int rew = rewField != null ? parse(rewField.getText()) : 0;
                int pen = penField != null ? parse(penField.getText()) : 0;

                if (rew > 0) task.getStatRewards().put(stat.getId(), rew);
                if (pen > 0) task.getStatPenalties().put(stat.getId(), pen);
            }
        }
    }

    private TextField createNumberField(Integer val, String prompt) {
        TextField tf = new TextField(val != null && val > 0 ? String.valueOf(val) : "");
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setPromptText(prompt);
        return tf;
    }

    private int parse(String text) {
        try { return Math.max(0, Integer.parseInt(text != null && !text.isEmpty() ? text.trim() : "0")); }
        catch (Exception e) { return 0; }
    }
}