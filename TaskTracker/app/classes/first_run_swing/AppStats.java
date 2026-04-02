package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.TaskItem;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private int currentStreak = 0;
    private LocalDate lastOpenedDate = LocalDate.now();
    private Map<LocalDate, Double> historyLog = new LinkedHashMap<>();

    // Phase 5 additions
    private String brainDumpText = "";
    private Map<TaskItem.OriginModule, String> pendingDrafts = new java.util.HashMap<>();

    // --- Getters & Setters ---
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public LocalDate getLastOpenedDate() { return lastOpenedDate; }
    public void setLastOpenedDate(LocalDate lastOpenedDate) { this.lastOpenedDate = lastOpenedDate; }

    public Map<LocalDate, Double> getHistoryLog() { return historyLog; }

    public void addHistoryRecord(LocalDate date, double completionPercentage) {
        historyLog.put(date, completionPercentage);
        if (historyLog.size() > 7) {
            LocalDate oldestDate = historyLog.keySet().iterator().next();
            historyLog.remove(oldestDate);
        }
    }

    public String getBrainDumpText() { return brainDumpText; }
    public void setBrainDumpText(String brainDumpText) { this.brainDumpText = brainDumpText; }

    public Map<TaskItem.OriginModule, String> getPendingDrafts() { return pendingDrafts; }
    public void saveDraft(TaskItem.OriginModule module, String text) {
        if (text != null && !text.trim().isEmpty()) {
            pendingDrafts.put(module, text.trim());
        } else {
            pendingDrafts.remove(module);
        }
    }
}