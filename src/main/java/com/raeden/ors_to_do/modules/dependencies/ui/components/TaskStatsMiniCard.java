package com.raeden.ors_to_do.modules.dependencies.ui.components;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class TaskStatsMiniCard extends FlowPane {

    private boolean containsStats = false;

    public TaskStatsMiniCard(TaskItem task, SectionConfig config, AppStats appStats, boolean isLocked) {
        super(10, 8);
        setPadding(new Insets(5, 0, 5, 0));

        if (isLocked) {
            setVisible(false);
            setManaged(false);
            return;
        }

        String baseLabelStyle = "-fx-font-size: 11px; -fx-padding: 2 6; -fx-background-radius: 10; -fx-border-radius: 10; -fx-font-weight: bold;";

        // --- NEW: Render Global Points FIRST (so they appear at the beginning) ---
        if (config != null && config.isEnableScore()) {
            if (task.getRewardPoints() > 0) {
                containsStats = true;
                Label ptsLabel = new Label("+" + task.getRewardPoints() + " Pts");
                ptsLabel.setStyle("-fx-text-fill: #FFD700; -fx-border-color: #FFD700; -fx-background-color: #332B00; " + baseLabelStyle);
                getChildren().add(ptsLabel);
            }
            if (task.getPenaltyPoints() > 0) {
                containsStats = true;
                Label penLabel = new Label("-" + task.getPenaltyPoints() + " Pts");
                penLabel.setStyle("-fx-text-fill: #FF6666; -fx-border-color: #FF6666; -fx-background-color: #331A1A; " + baseLabelStyle);
                getChildren().add(penLabel);
            }
        }

        // --- Render Custom RPG Stats ---
        if (config != null && config.isEnableStatsSystem() && appStats.isGlobalStatsEnabled()) {
            boolean isExpandedMode = appStats.isExpandStatMiniCards();

            for (CustomStat stat : appStats.getCustomStats()) {
                String statId = stat.getId();

                boolean hasReward = task.getStatRewards() != null && task.getStatRewards().containsKey(statId);
                boolean hasCapReward = task.getStatCapRewards() != null && task.getStatCapRewards().containsKey(statId);
                boolean hasCost = task.getStatCosts() != null && task.getStatCosts().containsKey(statId);
                boolean hasPenalty = task.getStatPenalties() != null && task.getStatPenalties().containsKey(statId);

                if (hasReward || hasCapReward || hasCost || hasPenalty) {
                    containsStats = true;

                    String statIcon = (stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None")) ? stat.getIconSymbol() + " " : "";
                    String bgColor = stat.getBackgroundColor() != null ? stat.getBackgroundColor() : "#333333";

                    String commonStyle = baseLabelStyle + " -fx-background-color: " + bgColor + ";";

                    String rewardStyle = "-fx-text-fill: #4EC9B0; -fx-border-color: #4EC9B0; " + commonStyle;
                    String capStyle = "-fx-text-fill: #C586C0; -fx-border-color: #C586C0; " + commonStyle;
                    String costStyle = "-fx-text-fill: #FF8C00; -fx-border-color: #FF8C00; " + commonStyle;
                    String penaltyStyle = "-fx-text-fill: #E06666; -fx-border-color: #E06666; " + commonStyle;

                    // 1. Render Cap Reward
                    if (hasCapReward) {
                        Label lbl;
                        if (isExpandedMode) {
                            lbl = new Label("▲ " + task.getStatCapRewards().get(statId) + " Max " + stat.getName());
                        } else {
                            lbl = new Label("▲ " + task.getStatCapRewards().get(statId) + " " + (statIcon.isEmpty() ? stat.getName() : statIcon.trim()));
                        }
                        lbl.setStyle(capStyle);
                        getChildren().add(lbl);
                    }

                    // 2. Render Reward
                    if (hasReward) {
                        Label lbl;
                        if (isExpandedMode) {
                            lbl = new Label("+" + task.getStatRewards().get(statId) + " " + stat.getName());
                        } else {
                            lbl = new Label("+" + task.getStatRewards().get(statId) + " " + (statIcon.isEmpty() ? stat.getName() : statIcon.trim()));
                        }
                        lbl.setStyle(rewardStyle);
                        getChildren().add(lbl);
                    }

                    // 3. Render Cost
                    if (hasCost) {
                        Label lbl;
                        if (isExpandedMode) {
                            lbl = new Label("~" + task.getStatCosts().get(statId) + " " + stat.getName());
                        } else {
                            lbl = new Label("~" + task.getStatCosts().get(statId) + " " + (statIcon.isEmpty() ? stat.getName() : statIcon.trim()));
                        }
                        lbl.setStyle(costStyle);
                        getChildren().add(lbl);
                    }

                    // 4. Render Penalty
                    if (hasPenalty) {
                        Label lbl;
                        if (isExpandedMode) {
                            lbl = new Label("-" + task.getStatPenalties().get(statId) + " " + stat.getName());
                        } else {
                            lbl = new Label("-" + task.getStatPenalties().get(statId) + " " + (statIcon.isEmpty() ? stat.getName() : statIcon.trim()));
                        }
                        lbl.setStyle(penaltyStyle);
                        getChildren().add(lbl);
                    }
                }
            }
        }

        if (!containsStats || !task.isStatsExpanded()) {
            setVisible(false);
            setManaged(false);
        }
    }

    public boolean hasAnyStats() {
        return containsStats;
    }
}