package com.raeden.ors_to_do.dependencies;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private int currentStreak = 0;
    private LocalDate lastOpenedDate = LocalDate.now();
    private Map<LocalDate, Double> historyLog = new LinkedHashMap<>();

    // --- Settings: Menu Texts ---
    private String editMenuText = "Edit Task";
    private String archiveMenuText = "Archive Task";
    private String deleteMenuText = "Delete";

    // --- Settings: Sidebar Texts ---
    private String navQuickText = "Quick To-Do";
    private String navDailyText = "Daily To-Do";
    private String navWorkText = "Work List";
    private String navFocusText = "Focus Hub";
    private String navArchiveText = "Archived";
    private String navSettingsText = "Settings";

    private List<TaskItem.CustomPriority> customPriorities = new ArrayList<>(List.of(
            new TaskItem.CustomPriority("LOW", "#4EC9B0"),
            new TaskItem.CustomPriority("MED", "#FF8C00"),
            new TaskItem.CustomPriority("HIGH", "#FF6666")
    ));

    private Map<LocalDate, int[]> advancedHistoryLog = new LinkedHashMap<>();
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

    public String getEditMenuText() { return editMenuText; }
    public void setEditMenuText(String editMenuText) { this.editMenuText = editMenuText; }
    public String getArchiveMenuText() { return archiveMenuText; }
    public void setArchiveMenuText(String archiveMenuText) { this.archiveMenuText = archiveMenuText; }
    public String getDeleteMenuText() { return deleteMenuText; }
    public void setDeleteMenuText(String deleteMenuText) { this.deleteMenuText = deleteMenuText; }

    public String getNavQuickText() { return navQuickText; }
    public void setNavQuickText(String navQuickText) { this.navQuickText = navQuickText; }
    public String getNavDailyText() { return navDailyText; }
    public void setNavDailyText(String navDailyText) { this.navDailyText = navDailyText; }
    public String getNavWorkText() { return navWorkText; }
    public void setNavWorkText(String navWorkText) { this.navWorkText = navWorkText; }
    public String getNavFocusText() { return navFocusText; }
    public void setNavFocusText(String navFocusText) { this.navFocusText = navFocusText; }
    public String getNavArchiveText() { return navArchiveText; }
    public void setNavArchiveText(String navArchiveText) { this.navArchiveText = navArchiveText; }
    public String getNavSettingsText() { return navSettingsText; }
    public void setNavSettingsText(String navSettingsText) { this.navSettingsText = navSettingsText; }

    public List<TaskItem.CustomPriority> getCustomPriorities() { return customPriorities; }
    public Map<LocalDate, int[]> getAdvancedHistoryLog() { return advancedHistoryLog; }

    public String getBrainDumpText() { return brainDumpText; }
    public void setBrainDumpText(String brainDumpText) { this.brainDumpText = brainDumpText; }

    public Map<TaskItem.OriginModule, String> getPendingDrafts() { return pendingDrafts; }
    public void saveDraft(TaskItem.OriginModule module, String text) {
        if (text != null && !text.trim().isEmpty()) pendingDrafts.put(module, text.trim());
        else pendingDrafts.remove(module);
    }
}