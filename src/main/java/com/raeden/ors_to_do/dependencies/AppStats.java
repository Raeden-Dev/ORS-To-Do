package com.raeden.ors_to_do.dependencies;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppStats implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- NEW: Dynamic Section Configuration ---
    public static class SectionConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;

        // Modular Toggles
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

        public SectionConfig(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

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
    }

    public static class DailyTemplate implements Serializable {
        private static final long serialVersionUID = 1L;
        private String prefix;
        private String text;
        private String prefixColor;
        private String bgColor;

        public DailyTemplate(String prefix, String text, String prefixColor, String bgColor) {
            this.prefix = prefix;
            this.text = text;
            this.prefixColor = prefixColor;
            this.bgColor = bgColor;
        }

        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getPrefixColor() { return prefixColor != null ? prefixColor : "#4EC9B0"; }
        public void setPrefixColor(String prefixColor) { this.prefixColor = prefixColor; }
        public String getBgColor() { return bgColor; }
        public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    }

    private int currentStreak = 0;
    private LocalDate lastOpenedDate = LocalDate.now();
    private Map<LocalDate, Double> historyLog = new LinkedHashMap<>();

    private String editMenuText = "Edit Task";
    private String archiveMenuText = "Archive Task";
    private String deleteMenuText = "Delete";
    private int taskFontSize = 14;

    private Boolean runInBackground = null;
    private Boolean matchDailyRectColor = null;
    private int minDailyCompletionPercent = 100;

    private String navQuickText = "Quick To-Do";
    private String navDailyText = "Daily To-Do";
    private String navWorkText = "Work List";
    private String navFocusText = "Focus Hub";
    private String navArchiveText = "Archived";
    private String navSettingsText = "Settings";

    // --- NEW: Section List ---
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

    // --- NEW: Section Getters ---
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
    public String getNavArchiveText() { return navArchiveText; }
    public void setNavArchiveText(String navArchiveText) { this.navArchiveText = navArchiveText; }
    public String getNavSettingsText() { return navSettingsText; }
    public void setNavSettingsText(String navSettingsText) { this.navSettingsText = navSettingsText; }
    public Map<LocalDate, int[]> getAdvancedHistoryLog() { return advancedHistoryLog; }
    public String getBrainDumpText() { return brainDumpText; }
    public void setBrainDumpText(String brainDumpText) { this.brainDumpText = brainDumpText; }

    public Map<TaskItem.OriginModule, String> getPendingDrafts() { return pendingDrafts; }
    public void saveDraft(TaskItem.OriginModule module, String text) {
        if (text != null && !text.trim().isEmpty()) { pendingDrafts.put(module, text.trim()); }
        else { pendingDrafts.remove(module); }
    }
}