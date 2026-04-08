package com.raeden.ors_to_do.modules.dependencies.services;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationManager {
    private static LocalDateTime lastInteractionTime = LocalDateTime.now();
    private static boolean notifiedInactivity = false;
    private static Timeline timeline;

    // Tracks the current active block for timed sections so we don't spam notifications
    private static Map<String, Long> sectionBlockIndices = new HashMap<>();
    private static Map<String, Integer> sectionNotifiedThresholds = new HashMap<>();

    // --- FIXED: In-memory tracker for dynamic deadline notifications ---
    private static Map<String, LocalDateTime> lastTaskNotificationTime = new HashMap<>();

    public static void start(AppStats appStats, List<TaskItem> db, Stage stage) {
        if (timeline != null) timeline.stop();

        // 1. Track user interaction via window focus for the Inactivity Warning
        stage.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastInteractionTime = LocalDateTime.now();
                notifiedInactivity = false;
            }
        });

        // 2. Check the clock every 1 minute
        timeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> checkNotifications(appStats, db)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private static void checkNotifications(AppStats appStats, List<TaskItem> db) {
        if (!appStats.isEnableNotifications()) return;

        LocalDateTime now = LocalDateTime.now();

        // --- CHECK 1: Inactivity (> 4 hours away) ---
        if (!notifiedInactivity && java.time.Duration.between(lastInteractionTime, now).toHours() >= 4) {
            Map<String, Integer> counts = new HashMap<>();
            for(TaskItem t : db) {
                if(!t.isFinished() && !t.isArchived() && !t.isOptional() && t.getSectionId() != null) {
                    counts.put(t.getSectionId(), counts.getOrDefault(t.getSectionId(), 0) + 1);
                }
            }

            String maxSectionId = null;
            int maxCount = 0;
            for(Map.Entry<String, Integer> e : counts.entrySet()) {
                if(e.getValue() > maxCount) {
                    maxCount = e.getValue();
                    maxSectionId = e.getKey();
                }
            }

            if(maxSectionId != null) {
                String sName = "a section";
                for(SectionConfig sc : appStats.getSections()) {
                    if(sc.getId().equals(maxSectionId)) sName = sc.getName();
                }
                SystemTrayManager.pushNotification("Time to focus!", "You've been away for a while. You still have " + maxCount + " tasks left in " + sName + ".");
            }
            notifiedInactivity = true;
        }

        // --- FIXED CHECK 2: Upcoming Deadlines (Dynamic Frequency) ---
        int freqHours = appStats.getDeadlineNotificationFrequencyHours();

        for(TaskItem t : db) {
            if(t.isFinished() || t.isArchived() || t.getDeadline() == null) continue;

            long minutesLeft = java.time.Duration.between(now, t.getDeadline()).toMinutes();
            if (minutesLeft <= 0) continue; // Skip if already expired

            double hoursLeft = minutesLeft / 60.0;

            // Only start notifying if the deadline is within the next 24 hours
            if (hoursLeft <= 24) {
                LocalDateTime lastNotified = lastTaskNotificationTime.get(t.getId());
                boolean shouldNotify = false;

                if (lastNotified == null) {
                    shouldNotify = true;
                } else {
                    long hoursSinceLastNotification = java.time.Duration.between(lastNotified, now).toHours();
                    if (hoursSinceLastNotification >= freqHours) {
                        shouldNotify = true;
                    }
                }

                if (shouldNotify) {
                    long h = minutesLeft / 60;
                    long m = minutesLeft % 60;
                    String timeLeftStr = (h > 0 ? h + "h " : "") + m + "m left";

                    SystemTrayManager.pushNotification("Deadline Approaching", timeLeftStr + " for: " + t.getTextContent());
                    lastTaskNotificationTime.put(t.getId(), now);
                }
            }
        }

        // --- CHECK 3: Timed Section Warnings (75%, 50%, 25%, 10%) ---
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        long totalMinutesSinceMidnight = java.time.Duration.between(startOfDay, now).toMinutes();

        for(SectionConfig sc : appStats.getSections()) {
            if(sc.getResetIntervalHours() <= 0) continue;

            long activeCount = db.stream().filter(t -> sc.getId().equals(t.getSectionId()) && !t.isFinished() && !t.isArchived() && !t.isOptional()).count();
            if(activeCount == 0) continue; // Skip if no tasks left!

            long intervalMinutes = sc.getResetIntervalHours() * 60;
            long currentBlockIndex = totalMinutesSinceMidnight / intervalMinutes;
            long minutesIntoCurrentBlock = totalMinutesSinceMidnight % intervalMinutes;
            long minutesRemaining = intervalMinutes - minutesIntoCurrentBlock;

            double pctRemaining = (double) minutesRemaining / intervalMinutes;

            // Reset threshold tracker if we entered a new time block
            long lastBlock = sectionBlockIndices.getOrDefault(sc.getId(), -1L);
            if(currentBlockIndex != lastBlock) {
                sectionBlockIndices.put(sc.getId(), currentBlockIndex);
                sectionNotifiedThresholds.put(sc.getId(), 100);
            }

            int lastThresh = sectionNotifiedThresholds.getOrDefault(sc.getId(), 100);
            int triggerThresh = -1;

            if (pctRemaining <= 0.10 && lastThresh > 10) triggerThresh = 10;
            else if (pctRemaining <= 0.25 && lastThresh > 25) triggerThresh = 25;
            else if (pctRemaining <= 0.50 && lastThresh > 50) triggerThresh = 50;
            else if (pctRemaining <= 0.75 && lastThresh > 75) triggerThresh = 75;

            if (triggerThresh != -1) {
                sectionNotifiedThresholds.put(sc.getId(), triggerThresh);
                SystemTrayManager.pushNotification(sc.getName() + " Reminder", triggerThresh + "% time left until reset! You have " + activeCount + " tasks remaining.");
            }
        }
    }
}