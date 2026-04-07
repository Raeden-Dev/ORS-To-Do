package com.raeden.ors_to_do.modules.dependencies.ui.utils;

import com.raeden.ors_to_do.dependencies.models.*;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;

import java.util.List;
import java.util.Map;

import static com.raeden.ors_to_do.modules.dependencies.services.SystemTrayManager.pushNotification;

public class TaskActionHandler {

    // --- FIXED: Added SectionConfig to parameters to check if stats are enabled ---
    public static void handleRewardPurchase(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        if (appStats.getGlobalScore() < task.getCostPoints()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Not enough points! You need " + task.getCostPoints() + " but only have " + appStats.getGlobalScore() + ".");
            alert.setHeaderText("Cannot Buy Reward");
            TaskDialogs.styleDialog(alert);
            alert.show();
            return;
        }

        // --- NEW: Check what stats this reward gives and show it in the confirmation ---
        StringBuilder modifierStr = new StringBuilder();
        if (config != null && config.isEnableStatsSystem()) {
            for (CustomStat stat : appStats.getCustomStats()) {
                int capAmt = getStatValue(task.getStatCapRewards(), stat);
                if (capAmt > 0) modifierStr.append("▲ ").append(capAmt).append(" Max ").append(stat.getName()).append(" Cap\n");

                int rewardAmt = getStatValue(task.getStatRewards(), stat);
                if (rewardAmt > 0) modifierStr.append("+").append(rewardAmt).append(" ").append(stat.getName()).append(" XP\n");

                int costAmt = getStatValue(task.getStatCosts(), stat);
                if (costAmt > 0) modifierStr.append("-").append(costAmt).append(" ").append(stat.getName()).append(" XP (Cost)\n");
            }
        }

        String statInfo = modifierStr.toString().trim();
        String prompt = "Buy '" + task.getTextContent() + "' for " + task.getCostPoints() + " points?" +
                (statInfo.isEmpty() ? "" : "\n\nYou will also gain:\n" + statInfo);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, prompt, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Purchase");
        TaskDialogs.styleDialog(alert);

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                appStats.setGlobalScore(appStats.getGlobalScore() - task.getCostPoints());
                appStats.setLifetimePointsSpent(appStats.getLifetimePointsSpent() + task.getCostPoints());
                appStats.setRewardsClaimed(appStats.getRewardsClaimed() + 1);

                if (task.isCounterMode()) {
                    task.setCurrentCount(task.getCurrentCount() + 1);
                    if (task.getMaxCount() > 0 && task.getCurrentCount() >= task.getMaxCount()) {
                        task.setFinished(true);
                    }
                } else {
                    task.setFinished(true);
                }

                // --- NEW: Process the stats from the reward ---
                if (config != null && config.isEnableStatsSystem()) {
                    processRPGStats(task, appStats, true);
                }

                StorageManager.saveStats(appStats);
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
                pushNotification("Reward Claimed!", "You bought: " + task.getTextContent());
            }
        });
    }

    public static void handleTaskCompletion(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, CheckBox optCheckBox) {
        boolean hasRewards = task.getRewardPoints() > 0 || (config.isEnableStatsSystem() && task.getStatRewards() != null && !task.getStatRewards().isEmpty());
        boolean hasCosts = config.isEnableStatsSystem() && task.getStatCosts() != null && !task.getStatCosts().isEmpty();
        boolean hasCapRewards = config.isEnableStatsSystem() && task.getStatCapRewards() != null && !task.getStatCapRewards().isEmpty();
        boolean hasModifiers = hasRewards || hasCosts || hasCapRewards;

        List<String> reqSections = appStats.getRequireConfirmationSections();

        boolean needsConfirmation = (hasModifiers && task.isPermaLock()) || reqSections.contains("ALL") || reqSections.contains(config.getId());

        if (needsConfirmation && !task.isPointsClaimed()) {

            String promptText;
            String titleText;

            if (hasModifiers) {
                StringBuilder modifierStr = new StringBuilder();
                if (task.getRewardPoints() > 0) modifierStr.append("+").append(task.getRewardPoints()).append(" Global Points\n");

                if (config.isEnableStatsSystem()) {
                    for (CustomStat stat : appStats.getCustomStats()) {
                        int capAmt = getStatValue(task.getStatCapRewards(), stat);
                        if (capAmt > 0) modifierStr.append("▲ ").append(capAmt).append(" Max ").append(stat.getName()).append(" Cap\n");

                        int rewardAmt = getStatValue(task.getStatRewards(), stat);
                        if (rewardAmt > 0) modifierStr.append("+").append(rewardAmt).append(" ").append(stat.getName()).append(" XP\n");

                        int costAmt = getStatValue(task.getStatCosts(), stat);
                        if (costAmt > 0) modifierStr.append("-").append(costAmt).append(" ").append(stat.getName()).append(" XP (Cost)\n");
                    }
                }
                titleText = "Complete Task & Process Stats";
                promptText = "Process the following changes?\n\n" + modifierStr.toString().trim() +
                        (task.isPermaLock() ? "\n\nThis will permanently lock the task." : "");
            } else {
                titleText = "Confirm Task Completion";
                promptText = "Are you sure you want to mark '" + task.getTextContent() + "' as completed?";
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, promptText, ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(titleText);
            TaskDialogs.styleDialog(alert);

            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    finalizeCompletion(task, appStats, config, hasModifiers);
                    StorageManager.saveStats(appStats);
                    StorageManager.saveTasks(globalDatabase);
                    onUpdate.run();
                } else {
                    if (optCheckBox != null) optCheckBox.setSelected(false);
                    if (task.isCounterMode()) task.setCurrentCount(task.getCurrentCount() - 1);
                    onUpdate.run();
                }
            });
        } else {
            finalizeCompletion(task, appStats, config, hasModifiers);
            StorageManager.saveTasks(globalDatabase);
            onUpdate.run();
        }
    }

    private static void finalizeCompletion(TaskItem task, AppStats appStats, SectionConfig config, boolean hasModifiers) {
        task.setFinished(true);

        if (task.isCounterMode() && task.getMaxCount() > 0) task.setCurrentCount(task.getMaxCount());
        for (SubTask sub : task.getSubTasks()) sub.setFinished(true);

        if (hasModifiers && !task.isPointsClaimed()) {
            task.setPointsClaimed(true);

            appStats.setGlobalScore(appStats.getGlobalScore() + task.getRewardPoints());
            if (config.isEnableStatsSystem()) {
                processRPGStats(task, appStats, true);
            }
        }
    }

    public static void processRPGStats(TaskItem task, AppStats appStats, boolean isCompletion) {
        if (!appStats.isGlobalStatsEnabled()) return;

        for (CustomStat stat : appStats.getCustomStats()) {
            int capAmt = getStatValue(task.getStatCapRewards(), stat);
            int rewardAmt = getStatValue(task.getStatRewards(), stat);
            int costAmt = getStatValue(task.getStatCosts(), stat);

            if (isCompletion) {
                // 1. Apply Max Cap Increases FIRST
                if (capAmt > 0 && stat.getMaxCap() > 0) {
                    stat.setMaxCap(stat.getMaxCap() + capAmt);
                }

                // 2. Apply Rewards (+XP)
                if (rewardAmt > 0) {
                    int newAmount = stat.getCurrentAmount() + rewardAmt;
                    if (stat.getMaxCap() > 0 && newAmount > stat.getMaxCap()) {
                        newAmount = stat.getMaxCap();
                    }
                    stat.setCurrentAmount(newAmount);
                    stat.setLifetimeEarned(stat.getLifetimeEarned() + rewardAmt);
                    if (stat.getCurrentAmount() > stat.getMaxLevelReached()) {
                        stat.setMaxLevelReached(stat.getCurrentAmount());
                    }
                    appStats.getLastStatGainDates().put(stat.getId(), java.time.LocalDate.now());
                }

                // 3. Apply Completion Costs (-XP)
                if (costAmt > 0) {
                    stat.setCurrentAmount(Math.max(0, stat.getCurrentAmount() - costAmt));
                    stat.setLifetimeLost(stat.getLifetimeLost() + costAmt);
                }

            } else {
                // Rollback Logic

                // 1. Remove Rewards (AND add to Lifetime Lost)
                if (rewardAmt > 0) {
                    stat.setCurrentAmount(Math.max(0, stat.getCurrentAmount() - rewardAmt));
                    stat.setLifetimeLost(stat.getLifetimeLost() + rewardAmt);
                }

                // 2. Refund Costs
                if (costAmt > 0) {
                    int newAmount = stat.getCurrentAmount() + costAmt;
                    if (stat.getMaxCap() > 0 && newAmount > stat.getMaxCap()) {
                        newAmount = stat.getMaxCap();
                    }
                    stat.setCurrentAmount(newAmount);
                }

                // 3. Rollback Max Cap Increases LAST
                if (capAmt > 0 && stat.getMaxCap() > 0) {
                    stat.setMaxCap(Math.max(1, stat.getMaxCap() - capAmt));

                    if (stat.getCurrentAmount() > stat.getMaxCap()) {
                        stat.setCurrentAmount(stat.getMaxCap());
                    }
                }
            }
        }
    }

    private static int getStatValue(Map<String, Integer> map, CustomStat stat) {
        if (map == null || map.isEmpty()) return 0;
        if (map.containsKey(stat.getId())) return map.get(stat.getId());
        if (map.containsKey(stat.getName())) return map.get(stat.getName());
        return 0;
    }
}