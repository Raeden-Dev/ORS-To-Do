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

    public static void handleRewardPurchase(TaskItem task, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        if (appStats.getGlobalScore() < task.getCostPoints()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Not enough points! You need " + task.getCostPoints() + " but only have " + appStats.getGlobalScore() + ".");
            alert.setHeaderText("Cannot Buy Reward");
            TaskDialogs.styleDialog(alert);
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Buy '" + task.getTextContent() + "' for " + task.getCostPoints() + " points?", ButtonType.YES, ButtonType.NO);
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
                }

                StorageManager.saveStats(appStats);
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
                pushNotification("Reward Claimed!", "You bought: " + task.getTextContent());
            }
        });
    }

    public static void handleTaskCompletion(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, CheckBox optCheckBox) {
        boolean hasRewards = task.getRewardPoints() > 0 || (config.isEnableStatsSystem() && !task.getStatRewards().isEmpty());

        List<String> reqSections = appStats.getRequireConfirmationSections();
        boolean needsConfirmation = hasRewards || reqSections.contains("ALL") || reqSections.contains(config.getId());

        if (needsConfirmation && !task.isPointsClaimed()) {

            String promptText;
            String titleText;

            if (hasRewards) {
                StringBuilder rewardStr = new StringBuilder();
                if (task.getRewardPoints() > 0) rewardStr.append("+").append(task.getRewardPoints()).append(" Global Points\n");

                if (config.isEnableStatsSystem()) {
                    for (CustomStat stat : appStats.getCustomStats()) {
                        int amt = task.getStatRewards().getOrDefault(stat.getId(), 0);
                        if (amt > 0) rewardStr.append("+").append(amt).append(" ").append(stat.getName()).append(" XP\n");
                    }
                }
                titleText = "Complete Task & Claim Rewards";
                promptText = "Claim the following rewards?\n\n" + rewardStr.toString().trim() + "\n\nThis will permanently lock the task.";
            } else {
                titleText = "Confirm Task Completion";
                promptText = "Are you sure you want to mark '" + task.getTextContent() + "' as completed?";
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, promptText, ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(titleText);
            TaskDialogs.styleDialog(alert);

            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    finalizeCompletion(task, appStats, config, hasRewards);
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
            finalizeCompletion(task, appStats, config, hasRewards);
            StorageManager.saveTasks(globalDatabase);
            onUpdate.run();
        }
    }

    private static void finalizeCompletion(TaskItem task, AppStats appStats, SectionConfig config, boolean hasRewards) {
        task.setFinished(true);
        if (hasRewards) {
            task.setPointsClaimed(true);
        }

        if (task.isCounterMode() && task.getMaxCount() > 0) task.setCurrentCount(task.getMaxCount());
        for (SubTask sub : task.getSubTasks()) sub.setFinished(true);

        if (task.isPointsClaimed()) {
            appStats.setGlobalScore(appStats.getGlobalScore() + task.getRewardPoints());
            if (config.isEnableStatsSystem()) {
                // --- FIXED: Triggering full RPG Engine instead of legacy method ---
                processRPGStats(task, appStats, true);
            }
        }
    }

    public static void processRPGStats(TaskItem task, AppStats appStats, boolean isCompletion) {
        Map<String, Integer> statChanges = isCompletion ? task.getStatRewards() : task.getStatPenalties();

        if (statChanges == null || statChanges.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : statChanges.entrySet()) {
            String statName = entry.getKey();
            int amount = entry.getValue();

            for (CustomStat stat : appStats.getCustomStats()) {
                // Notice we match by ID now to be safe, but fallback to name just in case
                if (stat.getId().equals(statName) || stat.getName().equals(statName)) {

                    if (isCompletion) {
                        int newAmount = stat.getCurrentAmount() + amount;

                        if (stat.getMaxCap() > 0 && newAmount > stat.getMaxCap()) {
                            newAmount = stat.getMaxCap();
                        }

                        stat.setCurrentAmount(newAmount);
                        stat.setLifetimeEarned(stat.getLifetimeEarned() + amount);

                        if (stat.getCurrentAmount() > stat.getMaxLevelReached()) {
                            stat.setMaxLevelReached(stat.getCurrentAmount());
                        }

                        appStats.getLastStatGainDates().put(stat.getId(), java.time.LocalDate.now());

                    } else {
                        int newAmount = Math.max(0, stat.getCurrentAmount() - amount);
                        stat.setCurrentAmount(newAmount);
                        stat.setLifetimeLost(stat.getLifetimeLost() + amount);
                    }
                    break;
                }
            }
        }
    }
}