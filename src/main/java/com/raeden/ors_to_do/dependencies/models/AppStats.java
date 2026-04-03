package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AppStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private int globalScore = 0;
    private int currentStreak = 0;
    private int highestStreak = 0;

    private int lifetimeDeletedTasks = 0;
    private boolean matchPriorityOutline = true;

    private int lifetimePointsSpent = 0;
    private int rewardsClaimed = 0;

    public int getLifetimePointsSpent() { return lifetimePointsSpent; }
    public void setLifetimePointsSpent(int lifetimePointsSpent) { this.lifetimePointsSpent = lifetimePointsSpent; }
    public int getRewardsClaimed() { return rewardsClaimed; }
    public void setRewardsClaimed(int rewardsClaimed) { this.rewardsClaimed = rewardsClaimed; }

    private LocalDate lastOpenedDate = LocalDate.now();
    private Map<LocalDate, Double> historyLog = new LinkedHashMap<>();

    private String editMenuText = "Edit Task";
    private String archiveMenuText = "Archive Task";
    private String deleteMenuText = "Delete";
    private int taskFontSize = 14;

    private Boolean runInBackground = null;
    private Boolean matchDailyRectColor = null;
    private int minDailyCompletionPercent = 100;
    private boolean matchTitleColor = false;
    private boolean alwaysOnTop = false;

    private int zenModeThreshold = 20;
    public int getZenModeThreshold() { return zenModeThreshold <= 0 ? 20 : zenModeThreshold; }
    public void setZenModeThreshold(int zenModeThreshold) { this.zenModeThreshold = zenModeThreshold; }

    public boolean isAlwaysOnTop() { return alwaysOnTop; }
    public void setAlwaysOnTop(boolean alwaysOnTop) { this.alwaysOnTop = alwaysOnTop; }
    public boolean isMatchTitleColor() { return matchTitleColor; }
    public void setMatchTitleColor(boolean matchTitleColor) { this.matchTitleColor = matchTitleColor; }

    private String navQuickText = "Quick To-Do";
    private String navDailyText = "Daily To-Do";
    private String navWorkText = "Work List";
    private String navFocusText = "Focus Hub";
    private String navAnalyticsText = "Analytics";
    private String navArchiveText = "Archived";
    private String navSettingsText = "Settings";

    private String navFocusColor = "#E06666";
    private String navAnalyticsColor = "#F2C94C";
    private String navArchiveColor = "#C586C0";
    private String navSettingsColor = "#858585";

    private LocalDateTime analyticsResetTimestamp;

    public LocalDateTime getAnalyticsResetTimestamp() { return analyticsResetTimestamp; }
    public void setAnalyticsResetTimestamp(LocalDateTime analyticsResetTimestamp) { this.analyticsResetTimestamp = analyticsResetTimestamp; }

    private List<SectionConfig> sections = new ArrayList<>();

    private List<CustomPriority> customPriorities = new ArrayList<>(List.of(
            new CustomPriority("LOW", "#4EC9B0"),
            new CustomPriority("MED", "#FF8C00"),
            new CustomPriority("HIGH", "#FF6666")
    ));

    private List<DailyTemplate> baseDailies = new ArrayList<>();
    private Map<LocalDate, int[]> advancedHistoryLog = new LinkedHashMap<>();
    private String brainDumpText = "";
    private Map<OriginModule, String> pendingDrafts = new HashMap<>();

    public int getGlobalScore() { return globalScore; }
    public void setGlobalScore(int globalScore) { this.globalScore = globalScore; }

    public int getHighestStreak() { return highestStreak; }
    public void setHighestStreak(int highestStreak) { this.highestStreak = highestStreak; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
        if (this.currentStreak > this.highestStreak) this.highestStreak = this.currentStreak;
    }

    public int getLifetimeDeletedTasks() { return lifetimeDeletedTasks; }
    public void setLifetimeDeletedTasks(int lifetimeDeletedTasks) { this.lifetimeDeletedTasks = lifetimeDeletedTasks; }

    public boolean isMatchPriorityOutline() { return matchPriorityOutline; }
    public void setMatchPriorityOutline(boolean matchPriorityOutline) { this.matchPriorityOutline = matchPriorityOutline; }

    public List<SectionConfig> getSections() {
        if (sections == null) sections = new ArrayList<>();
        return sections;
    }

    public int getMinDailyCompletionPercent() { return minDailyCompletionPercent < 10 ? 100 : minDailyCompletionPercent; }
    public void setMinDailyCompletionPercent(int minDailyCompletionPercent) { this.minDailyCompletionPercent = minDailyCompletionPercent; }
    public boolean isRunInBackground() { return runInBackground == null ? true : runInBackground; }
    public void setRunInBackground(boolean runInBackground) { this.runInBackground = runInBackground; }
    public boolean isMatchDailyRectColor() { return matchDailyRectColor != null && matchDailyRectColor; }
    public void setMatchDailyRectColor(boolean matchDailyRectColor) { this.matchDailyRectColor = matchDailyRectColor; }
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

    public List<DailyTemplate> getBaseDailies() {
        if (baseDailies == null) baseDailies = new ArrayList<>();
        return baseDailies;
    }

    public List<CustomPriority> getCustomPriorities() {
        if (customPriorities == null) customPriorities = new ArrayList<>();
        return customPriorities;
    }

    public int getTaskFontSize() { return taskFontSize == 0 ? 14 : taskFontSize; }
    public void setTaskFontSize(int taskFontSize) { this.taskFontSize = taskFontSize; }
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
    public String getNavAnalyticsText() { return navAnalyticsText != null ? navAnalyticsText : "Analytics"; }
    public void setNavAnalyticsText(String navAnalyticsText) { this.navAnalyticsText = navAnalyticsText; }
    public String getNavArchiveText() { return navArchiveText; }
    public void setNavArchiveText(String navArchiveText) { this.navArchiveText = navArchiveText; }
    public String getNavSettingsText() { return navSettingsText; }
    public void setNavSettingsText(String navSettingsText) { this.navSettingsText = navSettingsText; }

    public String getNavFocusColor() { return navFocusColor != null ? navFocusColor : "#E06666"; }
    public void setNavFocusColor(String navFocusColor) { this.navFocusColor = navFocusColor; }
    public String getNavAnalyticsColor() { return navAnalyticsColor != null ? navAnalyticsColor : "#F2C94C"; }
    public void setNavAnalyticsColor(String navAnalyticsColor) { this.navAnalyticsColor = navAnalyticsColor; }
    public String getNavArchiveColor() { return navArchiveColor != null ? navArchiveColor : "#C586C0"; }
    public void setNavArchiveColor(String navArchiveColor) { this.navArchiveColor = navArchiveColor; }
    public String getNavSettingsColor() { return navSettingsColor != null ? navSettingsColor : "#858585"; }
    public void setNavSettingsColor(String navSettingsColor) { this.navSettingsColor = navSettingsColor; }

    public Map<LocalDate, int[]> getAdvancedHistoryLog() { return advancedHistoryLog; }
    public String getBrainDumpText() { return brainDumpText; }
    public void setBrainDumpText(String brainDumpText) { this.brainDumpText = brainDumpText; }

    public Map<OriginModule, String> getPendingDrafts() { return pendingDrafts; }
    public void saveDraft(OriginModule module, String text) {
        if (text != null && !text.trim().isEmpty()) { pendingDrafts.put(module, text.trim()); }
        else { pendingDrafts.remove(module); }
    }

    public void copyFrom(AppStats other) {
        this.globalScore = other.globalScore;
        this.currentStreak = other.currentStreak;
        this.highestStreak = other.highestStreak;
        this.lifetimeDeletedTasks = other.lifetimeDeletedTasks;
        this.matchPriorityOutline = other.matchPriorityOutline;
        this.lastOpenedDate = other.lastOpenedDate;
        this.historyLog = new LinkedHashMap<>(other.historyLog);
        this.editMenuText = other.editMenuText;
        this.archiveMenuText = other.archiveMenuText;
        this.deleteMenuText = other.deleteMenuText;
        this.taskFontSize = other.taskFontSize;
        this.runInBackground = other.runInBackground;
        this.matchDailyRectColor = other.matchDailyRectColor;
        this.minDailyCompletionPercent = other.minDailyCompletionPercent;
        this.navQuickText = other.navQuickText;
        this.navDailyText = other.navDailyText;
        this.navWorkText = other.navWorkText;
        this.navFocusText = other.navFocusText;
        this.navAnalyticsText = other.navAnalyticsText;
        this.navArchiveText = other.navArchiveText;
        this.navSettingsText = other.navSettingsText;
        this.navFocusColor = other.navFocusColor;
        this.navAnalyticsColor = other.navAnalyticsColor;
        this.navArchiveColor = other.navArchiveColor;
        this.navSettingsColor = other.navSettingsColor;
        this.sections = new ArrayList<>(other.sections);
        this.customPriorities = new ArrayList<>(other.customPriorities);
        this.baseDailies = new ArrayList<>(other.baseDailies);
        this.advancedHistoryLog = new LinkedHashMap<>(other.advancedHistoryLog);
        this.brainDumpText = other.brainDumpText;
        this.pendingDrafts = new HashMap<>(other.pendingDrafts);
        this.analyticsResetTimestamp = other.analyticsResetTimestamp;
        this.matchTitleColor = other.matchTitleColor;
        this.alwaysOnTop = other.alwaysOnTop;
        this.zenModeThreshold = other.zenModeThreshold;
        this.lifetimePointsSpent = other.lifetimePointsSpent;
        this.rewardsClaimed = other.rewardsClaimed;
    }
}