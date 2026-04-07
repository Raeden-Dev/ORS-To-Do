package com.raeden.ors_to_do.modules.dependencies.ui.forms;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
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
    // --- NEW: Perma Lock Checkbox ---
    private CheckBox permaLockCheck;

    private Map<String, TextField> statRewardFields = new HashMap<>();

    // --- NEW: Cap Reward Fields ---
    private Map<String, TextField> statCapRewardFields = new HashMap<>();

    private Map<String, TextField> statCostFields = new HashMap<>();
    private Map<String, TextField> statPenaltyFields = new HashMap<>();

    public void buildUI(GridPane grid, AtomicInteger rowIdx, TaskItem task, SectionConfig config, AppStats appStats) {

        if (config != null && config.isEnableScore()) {
            grid.add(new Label("Global Points:"), 0, rowIdx.get());
            HBox scoreBox = new HBox(10);
            scoreBox.setAlignment(Pos.CENTER_LEFT);

            rewardField = new TextField(String.valueOf(task.getRewardPoints()));
            rewardField.setPromptText("+ Reward");
            rewardField.setPrefWidth(70);

            penaltyField = new TextField(String.valueOf(task.getPenaltyPoints()));
            penaltyField.setPromptText("- Penalty (Fail)");
            penaltyField.setPrefWidth(90);

            scoreBox.getChildren().addAll(new Label("+"), rewardField, new Label("- (Fail)"), penaltyField);
            grid.add(scoreBox, 1, rowIdx.getAndIncrement());
        }

        if (config != null && config.isRewardsPage()) {
            costField = new TextField(String.valueOf(task.getCostPoints()));
            costField.setPromptText("Cost");
            grid.add(new Label("Store Cost:"), 0, rowIdx.get());
            grid.add(costField, 1, rowIdx.getAndIncrement());
        }

        maxCountField = new TextField(task.getMaxCount() > 0 ? String.valueOf(task.getMaxCount()) : "");
        maxCountField.setPromptText("Target Count (Optional)");

        // --- NEW: Perma Lock Setup ---
        permaLockCheck = new CheckBox("Perma Lock");
        permaLockCheck.setStyle("-fx-text-fill: white;");
        permaLockCheck.setSelected(task.isPermaLock());

        // Only enable if counter mode has a value > 0
        permaLockCheck.setDisable(!task.isCounterMode());
        maxCountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                permaLockCheck.setDisable(Integer.parseInt(newVal.trim()) <= 0);
            } catch (NumberFormatException e) {
                permaLockCheck.setDisable(true);
                permaLockCheck.setSelected(false);
            }
        });

        HBox counterRow = new HBox(10, maxCountField, permaLockCheck);
        counterRow.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Counter Mode:"), 0, rowIdx.get());
        grid.add(counterRow, 1, rowIdx.getAndIncrement());

        if (config != null && config.isEnableStatsSystem() && appStats.isGlobalStatsEnabled() && !appStats.getCustomStats().isEmpty()) {
            grid.add(new Separator(), 0, rowIdx.get(), 2, 1); rowIdx.getAndIncrement();

            Label statsHeader = new Label("RPG Stat Modifiers");
            statsHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #B5CEA8;");
            grid.add(statsHeader, 0, rowIdx.get(), 2, 1); rowIdx.getAndIncrement();

            HBox headers = new HBox(10);
            headers.setAlignment(Pos.CENTER_LEFT);
            Label rLbl = new Label("+ Reward"); rLbl.setPrefWidth(60); rLbl.setStyle("-fx-text-fill: #4EC9B0;");

            // --- NEW: Added Max Cap Label ---
            Label cpLbl = new Label("+ Max Cap"); cpLbl.setPrefWidth(65); cpLbl.setStyle("-fx-text-fill: #C586C0;");

            Label cLbl = new Label("- Cost"); cLbl.setPrefWidth(60); cLbl.setStyle("-fx-text-fill: #FF8C00;");
            Label pLbl = new Label("- Penalty"); pLbl.setPrefWidth(60); pLbl.setStyle("-fx-text-fill: #E06666;");
            headers.getChildren().addAll(rLbl, cpLbl, cLbl, pLbl);
            grid.add(headers, 1, rowIdx.getAndIncrement());

            for (CustomStat stat : appStats.getCustomStats()) {
                Label statLabel = new Label(stat.getName() + ":");
                statLabel.setStyle("-fx-text-fill: " + (stat.getTextColor() != null ? stat.getTextColor() : "#FFFFFF") + ";");
                grid.add(statLabel, 0, rowIdx.get());

                HBox fieldBox = new HBox(10);
                fieldBox.setAlignment(Pos.CENTER_LEFT);

                TextField rField = new TextField();
                rField.setPrefWidth(60);
                if (task.getStatRewards() != null && task.getStatRewards().containsKey(stat.getId())) {
                    rField.setText(String.valueOf(task.getStatRewards().get(stat.getId())));
                }

                // --- NEW: Cap Field ---
                TextField cpField = new TextField();
                cpField.setPrefWidth(65);
                if (task.getStatCapRewards() != null && task.getStatCapRewards().containsKey(stat.getId())) {
                    cpField.setText(String.valueOf(task.getStatCapRewards().get(stat.getId())));
                }

                TextField cField = new TextField();
                cField.setPrefWidth(60);
                if (task.getStatCosts() != null && task.getStatCosts().containsKey(stat.getId())) {
                    cField.setText(String.valueOf(task.getStatCosts().get(stat.getId())));
                }

                TextField pField = new TextField();
                pField.setPrefWidth(60);
                if (task.getStatPenalties() != null && task.getStatPenalties().containsKey(stat.getId())) {
                    pField.setText(String.valueOf(task.getStatPenalties().get(stat.getId())));
                }

                statRewardFields.put(stat.getId(), rField);
                statCapRewardFields.put(stat.getId(), cpField);
                statCostFields.put(stat.getId(), cField);
                statPenaltyFields.put(stat.getId(), pField);

                fieldBox.getChildren().addAll(rField, cpField, cField, pField);
                grid.add(fieldBox, 1, rowIdx.getAndIncrement());
            }
        }
    }

    public void applyTo(TaskItem task) {
        if (rewardField != null) {
            try { task.setRewardPoints(Math.max(0, Integer.parseInt(rewardField.getText().trim()))); } catch (Exception ignore) {}
        }
        if (penaltyField != null) {
            try { task.setPenaltyPoints(Math.max(0, Integer.parseInt(penaltyField.getText().trim()))); } catch (Exception ignore) {}
        }
        if (costField != null) {
            try { task.setCostPoints(Math.max(0, Integer.parseInt(costField.getText().trim()))); } catch (Exception ignore) {}
        }

        // --- NEW: Extract and save Perma Lock ---
        if (maxCountField != null) {
            try {
                int max = Integer.parseInt(maxCountField.getText().trim());
                task.setMaxCount(Math.max(0, max));
                task.setCounterMode(max > 0);

                if (permaLockCheck != null) {
                    task.setPermaLock(permaLockCheck.isSelected());
                }
            } catch (Exception ignore) {
                task.setMaxCount(0);
                task.setCounterMode(false);
                if (permaLockCheck != null) task.setPermaLock(false);
            }
        }

        Map<String, Integer> rewards = new HashMap<>();
        Map<String, Integer> capRewards = new HashMap<>();
        Map<String, Integer> costs = new HashMap<>();
        Map<String, Integer> penalties = new HashMap<>();

        for (String statId : statRewardFields.keySet()) {
            try {
                int rVal = Integer.parseInt(statRewardFields.get(statId).getText().trim());
                if (rVal > 0) rewards.put(statId, rVal);
            } catch (Exception ignore) {}

            try {
                int cpVal = Integer.parseInt(statCapRewardFields.get(statId).getText().trim());
                if (cpVal > 0) capRewards.put(statId, cpVal);
            } catch (Exception ignore) {}

            try {
                int cVal = Integer.parseInt(statCostFields.get(statId).getText().trim());
                if (cVal > 0) costs.put(statId, cVal);
            } catch (Exception ignore) {}

            try {
                int pVal = Integer.parseInt(statPenaltyFields.get(statId).getText().trim());
                if (pVal > 0) penalties.put(statId, pVal);
            } catch (Exception ignore) {}
        }

        task.setStatRewards(rewards);
        task.setStatCapRewards(capRewards);
        task.setStatCosts(costs);
        task.setStatPenalties(penalties);
    }
}