package com.raeden.ors_to_do.dependencies;

import com.raeden.ors_to_do.dependencies.TaskItem;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppStats implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class SectionConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
        private String sidebarColor = "#569CD6";

        private int resetIntervalHours = 0;
        private boolean autoArchiveCompleted = false;
        private List<DailyTemplate> autoAddTemplates = new ArrayList<>();

        private boolean hasStreak = false;
        private boolean showAnalytics = false;
        private boolean enableSubTasks = false;
        private boolean showPriority = false;
        private boolean showDate = false;
        private boolean trackTime = false;
        private boolean showPrefix = false;
        private boolean showWorkType = false;
        private boolean allowArchive = false;
        private boolean showTags = false;
        private boolean allowFavorite = false;
        private boolean enableScore = false;
        // --- NEW: Links Toggle ---
        private boolean enableLinks = false;
        private boolean enableIcons;
        public boolean isEnableIcons() { return enableIcons; }
        public void setEnableIcons(boolean enableIcons) { this.enableIcons = enableIcons; }

        public SectionConfig(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSidebarColor() { return sidebarColor != null ? sidebarColor : "#569CD6"; }
        public void setSidebarColor(String sidebarColor) { this.sidebarColor = sidebarColor; }

        public int getResetIntervalHours() { return resetIntervalHours; }
        public void setResetIntervalHours(int resetIntervalHours) { this.resetIntervalHours = resetIntervalHours; }

        public boolean isAutoArchiveCompleted() { return autoArchiveCompleted; }
        public void setAutoArchiveCompleted(boolean autoArchiveCompleted) { this.autoArchiveCompleted = autoArchiveCompleted; }

        public List<DailyTemplate> getAutoAddTemplates() {
            if (autoAddTemplates == null) autoAddTemplates = new ArrayList<>();
            return autoAddTemplates;
        }

        public boolean isHasStreak() { return hasStreak; }
        public void setHasStreak(boolean hasStreak) { this.hasStreak = hasStreak; }
        public boolean isShowAnalytics() { return showAnalytics; }
        public void setShowAnalytics(boolean showAnalytics) { this.showAnalytics = showAnalytics; }
        public boolean isEnableSubTasks() { return enableSubTasks; }
        public void setEnableSubTasks(boolean enableSubTasks) { this.enableSubTasks = enableSubTasks; }
        public boolean isShowPriority() { return showPriority; }
        public void setShowPriority(boolean showPriority) { this.showPriority = showPriority; }
        public boolean isShowDate() { return showDate; }
        public void setShowDate(boolean showDate) { this.showDate = showDate; }
        public boolean isTrackTime() { return trackTime; }
        public void setTrackTime(boolean trackTime) { this.trackTime = trackTime; }
        public boolean isShowPrefix() { return showPrefix; }
        public void setShowPrefix(boolean showPrefix) { this.showPrefix = showPrefix; }
        public boolean isShowWorkType() { return showWorkType; }
        public void setShowWorkType(boolean showWorkType) { this.showWorkType = showWorkType; }
        public boolean isAllowArchive() { return allowArchive; }
        public void setAllowArchive(boolean allowArchive) { this.allowArchive = allowArchive; }
        public boolean isShowTags() { return showTags; }
        public void setShowTags(boolean showTags) { this.showTags = showTags; }
        public boolean isAllowFavorite() { return allowFavorite; }
        public void setAllowFavorite(boolean allowFavorite) { this.allowFavorite = allowFavorite; }
        public boolean isEnableScore() { return enableScore; }
        public void setEnableScore(boolean enableScore) { this.enableScore = enableScore; }
        public boolean isEnableLinks() { return enableLinks; }
        public void setEnableLinks(boolean enableLinks) { this.enableLinks = enableLinks; }
    }

    public static class DailyTemplate implements Serializable {
        private static final long serialVersionUID = 1L;
        private String prefix;
        private String text;
        private String prefixColor;
        private String bgColor;
        private List<java.time.DayOfWeek> activeDays;

        private String iconSymbol;
        private String iconColor;
        public String getIconSymbol() { return iconSymbol; }
        public void setIconSymbol(String iconSymbol) { this.iconSymbol = iconSymbol; }
        public String getIconColor() { return iconColor; }
        public void setIconColor(String iconColor) { this.iconColor = iconColor; }

        // --- NEW: Dynamic Feature Storage ---
        private String priorityName;
        private int rewardPoints;
        private int penaltyPoints;
        private String workType;
        private List<String> subTaskLines;

        public DailyTemplate(String prefix, String text, String prefixColor, String bgColor) {
            this.prefix = prefix;
            this.text = text;
            this.prefixColor = prefixColor;
            this.bgColor = bgColor;
            this.activeDays = new java.util.ArrayList<>(java.util.Arrays.asList(java.time.DayOfWeek.values()));
            this.subTaskLines = new java.util.ArrayList<>();
        }

        public String getPriorityName() { return priorityName; }
        public void setPriorityName(String priorityName) { this.priorityName = priorityName; }
        public int getRewardPoints() { return rewardPoints; }
        public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }
        public int getPenaltyPoints() { return penaltyPoints; }
        public void setPenaltyPoints(int penaltyPoints) { this.penaltyPoints = penaltyPoints; }
        public String getWorkType() { return workType; }
        public void setWorkType(String workType) { this.workType = workType; }

        public List<String> getSubTaskLines() {
            if (subTaskLines == null) subTaskLines = new java.util.ArrayList<>();
            return subTaskLines;
        }
        public void setSubTaskLines(List<String> subTaskLines) { this.subTaskLines = subTaskLines; }

        public List<java.time.DayOfWeek> getActiveDays() {
            if (activeDays == null) activeDays = new java.util.ArrayList<>(java.util.Arrays.asList(java.time.DayOfWeek.values()));
            return activeDays;
        }
        public void setActiveDays(List<java.time.DayOfWeek> activeDays) { this.activeDays = activeDays; }

        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getPrefixColor() { return prefixColor; }
        public void setPrefixColor(String prefixColor) { this.prefixColor = prefixColor; }
        public String getBgColor() { return bgColor; }
        public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    }

    private int globalScore = 0;
    private int currentStreak = 0;
    private int highestStreak = 0;

    private int lifetimeDeletedTasks = 0;
    private boolean matchPriorityOutline = true;

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
    public void setAnalyticsResetTimestamp(java.time.LocalDateTime analyticsResetTimestamp) { this.analyticsResetTimestamp = analyticsResetTimestamp; }

    private List<SectionConfig> sections = new ArrayList<>();

    private List<TaskItem.CustomPriority> customPriorities = new ArrayList<>(List.of(
            new TaskItem.CustomPriority("LOW", "#4EC9B0"),
            new TaskItem.CustomPriority("MED", "#FF8C00"),
            new TaskItem.CustomPriority("HIGH", "#FF6666")
    ));

    private List<DailyTemplate> baseDailies = new ArrayList<>();
    private Map<LocalDate, int[]> advancedHistoryLog = new LinkedHashMap<>();
    private String brainDumpText = "";
    private Map<TaskItem.OriginModule, String> pendingDrafts = new java.util.HashMap<>();

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

    public List<TaskItem.CustomPriority> getCustomPriorities() {
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

    public Map<TaskItem.OriginModule, String> getPendingDrafts() { return pendingDrafts; }
    public void saveDraft(TaskItem.OriginModule module, String text) {
        if (text != null && !text.trim().isEmpty()) { pendingDrafts.put(module, text.trim()); }
        else { pendingDrafts.remove(module); }
    }
    // --- NEW: Safe Copy Method for Data Restoration ---
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
        this.pendingDrafts = new java.util.HashMap<>(other.pendingDrafts);
        this.analyticsResetTimestamp = other.analyticsResetTimestamp;
        this.matchTitleColor = other.matchTitleColor;
    }
}