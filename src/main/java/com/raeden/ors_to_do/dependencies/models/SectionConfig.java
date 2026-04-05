package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SectionConfig implements Serializable {
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
    private boolean enableLinks = false;
    private boolean enableStatsSystem = false;

    // --- NEW: NOTES MODE FLAG ---
    private boolean isNotesPage = false;

    public boolean isNotesPage() { return isNotesPage; }
    public void setNotesPage(boolean isNotesPage) { this.isNotesPage = isNotesPage; }

    private boolean enableIcons;
    public boolean isEnableIcons() { return enableIcons; }
    public void setEnableIcons(boolean enableIcons) { this.enableIcons = enableIcons; }

    private boolean isRewardsPage = false;
    public boolean isRewardsPage() { return isRewardsPage; }
    public void setRewardsPage(boolean isRewardsPage) { this.isRewardsPage = isRewardsPage; }

    private boolean enableZenMode = false;
    public boolean isEnableZenMode() { return enableZenMode; }
    public void setEnableZenMode(boolean enableZenMode) { this.enableZenMode = enableZenMode; }

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
    public boolean isEnableStatsSystem() { return enableStatsSystem; }
    public void setEnableStatsSystem(boolean enableStatsSystem) { this.enableStatsSystem = enableStatsSystem; }
}