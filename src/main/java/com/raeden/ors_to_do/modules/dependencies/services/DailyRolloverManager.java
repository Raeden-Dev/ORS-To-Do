package com.raeden.ors_to_do.modules.dependencies.services;

import com.raeden.ors_to_do.dependencies.models.*;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;

import java.time.LocalDate;
import java.util.List;
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

                        // --- NEW: Apply dynamic config variables during automatic rollover ---
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

                        taskDatabase.add(newTask);
                    }
                }
            }

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
}