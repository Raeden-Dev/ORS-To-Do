package com.raeden.ors_to_do.modules.dependencies.services;

import com.raeden.ors_to_do.dependencies.models.*;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DailyRolloverManager {

    public static void processDailyRollover(AppStats appStats, List<TaskItem> taskDatabase) {
        LocalDate today = LocalDate.now();
        LocalDate lastOpened = appStats.getLastOpenedDate();

        if (today.isAfter(lastOpened)) {
            for (SectionConfig section : appStats.getSections()) {
                if (section.getResetIntervalHours() > 0) {

                    int totalDaily = 0;
                    int completedDaily = 0;

                    for (TaskItem task : taskDatabase) {
                        if (section.getId().equals(task.getSectionId()) && !task.isArchived()) {
                            totalDaily++;
                            if (task.isFinished()) completedDaily++;
                        }
                    }

                    if (section.isHasStreak() && totalDaily > 0) {
                        double percentComplete = (double) completedDaily / totalDaily;
                        appStats.addHistoryRecord(lastOpened, percentComplete);
                        appStats.getAdvancedHistoryLog().put(lastOpened, new int[]{totalDaily, completedDaily});

                        double requiredFraction = appStats.getMinDailyCompletionPercent() / 100.0;
                        if (percentComplete >= (requiredFraction - 0.001)) appStats.setCurrentStreak(appStats.getCurrentStreak() + 1);
                        else appStats.setCurrentStreak(0);
                    }

                    for (TaskItem task : taskDatabase) {
                        if (section.getId().equals(task.getSectionId()) && !task.isArchived()) {
                            task.setArchived(true);
                            if (task.getDateCompleted() == null) task.setFinished(true);
                        }
                    }

                    CustomPriority fallbackPrio = appStats.getCustomPriorities().isEmpty() ? null : appStats.getCustomPriorities().get(0);

                    for (DailyTemplate template : section.getAutoAddTemplates()) {
                        if (template.getActiveDays() != null && !template.getActiveDays().isEmpty() && !template.getActiveDays().contains(today.getDayOfWeek())) {
                            continue;
                        }

                        TaskItem newTask = new TaskItem(template.getText(), fallbackPrio, section.getId());

                        // --- Apply dynamic config variables during automatic rollover ---
                        if (section.isShowPrefix() && template.getPrefix() != null && !template.getPrefix().isEmpty()) {
                            newTask.setPrefix(template.getPrefix());
                            newTask.setPrefixColor(template.getPrefixColor());
                        }
                        if (section.isShowPriority() && template.getPriorityName() != null) {
                            appStats.getCustomPriorities().stream().filter(p -> p.getName().equals(template.getPriorityName())).findFirst().ifPresent(newTask::setPriority);
                        }
                        if (section.isShowTaskType() && template.getTaskType() != null) newTask.setTaskType(template.getTaskType());
                        if (section.isEnableScore()) {
                            newTask.setRewardPoints(template.getRewardPoints());
                            newTask.setPenaltyPoints(template.getPenaltyPoints());
                        }
                        if (section.isEnableSubTasks() && template.getSubTaskLines() != null) {
                            for (String st : template.getSubTaskLines()) {
                                if (!st.trim().isEmpty()) newTask.getSubTasks().add(new SubTask(st.trim()));
                            }
                        }
                        if (template.getBgColor() != null) newTask.setColorHex(template.getBgColor());
                        newTask.setOptional(template.isOptional());

                        taskDatabase.add(newTask);
                    }
                }
            }

            // --- PHASE 5: Trigger RPG Gamification Engine ---
            processRPGRollover(appStats, taskDatabase);

            appStats.setLastOpenedDate(today);
            StorageManager.saveStats(appStats);
            StorageManager.saveTasks(taskDatabase);
        }
    }

    public static void autoArchiveTasks(AppStats appStats, List<TaskItem> taskDatabase) {
        for (TaskItem task : taskDatabase) {
            Optional<SectionConfig> matchedConfig = appStats.getSections().stream()
                    .filter(c -> c.getId().equals(task.getSectionId()))
                    .findFirst();

            if (matchedConfig.isPresent() && matchedConfig.get().isAutoArchive()) {
                if (task.isFinished() && !task.isArchived() && !task.isCounterMode()) {
                    task.setArchived(true);
                    if (task.getDateCompleted() == null) task.setFinished(true);
                }
            }
        }
    }

    // --- PHASE 5: RPG ATROPHY AND PERK ENGINE ---
    public static void processRPGRollover(AppStats appStats, List<TaskItem> globalDatabase) {
        if (!appStats.isGlobalStatsEnabled()) return;

        LocalDate today = LocalDate.now();
        boolean statsChanged = false;
        boolean perksChanged = false;

        // ==========================================
        // 1. STAT ATROPHY ENGINE
        // ==========================================
        for (CustomStat stat : appStats.getCustomStats()) {
            if (stat.getAtrophyDays() > 0 && stat.getCurrentAmount() > 0) {

                // Get the last time this stat was trained (Default to today if it's a brand new stat)
                LocalDate lastGain = appStats.getLastStatGainDates().getOrDefault(stat.getId(), today);

                long daysSinceGain = ChronoUnit.DAYS.between(lastGain, today);

                if (daysSinceGain >= stat.getAtrophyDays()) {
                    // Decay the stat by 1 point
                    stat.setCurrentAmount(Math.max(0, stat.getCurrentAmount() - 1));
                    stat.setLifetimeLost(stat.getLifetimeLost() + 1);
                    statsChanged = true;

                    // Push the anchor date forward by 1 day so it continues to bleed 1 XP per day
                    // instead of draining entirely all at once.
                    appStats.getLastStatGainDates().put(stat.getId(), lastGain.plusDays(1));

                    // Send a warning to the user
                    SystemTrayManager.pushNotification(
                            "Stat Atrophy: " + stat.getName(),
                            "Your " + stat.getName() + " stat has decayed due to inactivity! Complete a task to recover it."
                    );
                }
            }
        }

        // ==========================================
        // 2. PERK LEVELING & UPKEEP ENGINE
        // ==========================================
        boolean isLevelUpDay = (today.getDayOfWeek() == DayOfWeek.MONDAY);

        for (TaskItem task : globalDatabase) {
            if (task.getStatRequirements() != null && !task.getStatRequirements().isEmpty()) {

                boolean meetsAllStats = true;

                // Check if current stats still satisfy the Perk's requirements
                for (Map.Entry<String, Integer> req : task.getStatRequirements().entrySet()) {
                    CustomStat s = appStats.getCustomStats().stream()
                            .filter(x -> x.getId().equals(req.getKey()))
                            .findFirst().orElse(null);

                    if (s == null || s.getCurrentAmount() < req.getValue()) {
                        meetsAllStats = false;
                        break;
                    }
                }

                if (meetsAllStats) {
                    // If maintained and it's weekly reset day, level up the perk!
                    if (isLevelUpDay && task.getPerkLevel() < 5) {
                        task.setWeeksMaintained(task.getWeeksMaintained() + 1);
                        task.setPerkLevel(task.getPerkLevel() + 1);
                        perksChanged = true;

                        SystemTrayManager.pushNotification(
                                "Perk Leveled Up! ✨",
                                "Your perk '" + task.getTextContent() + "' has reached Level " + task.getPerkLevel() + "!"
                        );
                    }
                } else {
                    // Stat atrophy caused stats to fall below requirements. Shatter the perk.
                    if (task.getPerkLevel() > 0) {
                        task.setPerkLevel(0);
                        task.setWeeksMaintained(0);
                        perksChanged = true;

                        SystemTrayManager.pushNotification(
                                "Perk Lost! 🔒",
                                "Stats decayed below requirements. You have lost the perk: " + task.getTextContent()
                        );
                    }
                }
            }
        }

        // Save if any changes occurred during the night (redundant to processDailyRollover's save, but safe)
        if (statsChanged) {
            StorageManager.saveStats(appStats);
        }
        if (perksChanged) {
            StorageManager.saveTasks(globalDatabase);
        }
    }
}