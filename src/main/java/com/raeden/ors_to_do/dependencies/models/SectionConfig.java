package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SectionConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    // ==========================================
    // 1. CORE INFORMATION & STYLING
    // ==========================================
    private String id;
    private String name;
    private String sidebarColor = "#569CD6";

    // ==========================================
    // 2. SPECIAL PAGE MODES
    // ==========================================
    private boolean isNotesPage = false;
    private boolean isRewardsPage = false;
    private boolean isStatPage = false;
    private boolean isPerkPage = false;
    // --- NEW: Challenge Page Mode ---
    private boolean isChallengePage = false;

    // ==========================================
    // 3. RESET & ARCHIVING LOGIC
    // ==========================================
    private int resetIntervalHours = 0;
    private boolean autoArchive = false;
    private boolean allowManualArchiving = false;
    private boolean hasStreak = false;

    private int currentStreak = 0;
    private int highestStreak = 0;

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
        if (this.currentStreak > this.highestStreak) this.highestStreak = this.currentStreak;
    }

    public int getHighestStreak() { return highestStreak; }
    public void setHighestStreak(int highestStreak) { this.highestStreak = highestStreak; }

    private boolean isSeparator = false;

    public boolean isSeparator() { return isSeparator; }
    public void setSeparator(boolean separator) { this.isSeparator = separator; }

    // ==========================================
    // 4. TASK FEATURE TOGGLES
    // ==========================================
    private boolean enableSubTasks = false;
    private boolean showPriority = false;
    private boolean showDate = false;
    private boolean trackTime = false;
    private boolean showPrefix = false;
    private boolean showWorkType = false;
    private boolean showTags = false;
    private boolean allowFavorite = false;
    private boolean enableScore = false;
    private boolean enableLinks = false;
    private boolean enableStatsSystem = false;
    private boolean enableLinkCards = true;
    private boolean enableIcons = false;
    private boolean enableZenMode = false;
    private boolean enableOptionalTasks = false;
    private boolean enableTaskStyling = false;
    private boolean showAnalytics = false;

    private boolean enableTimedTasks = false;

    // ==========================================
    // 5. LISTS & TEMPLATES
    // ==========================================
    private List<DailyTemplate> autoAddTemplates = new ArrayList<>();

    public SectionConfig(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSidebarColor() { return sidebarColor != null ? sidebarColor : "#569CD6"; }
    public void setSidebarColor(String sidebarColor) { this.sidebarColor = sidebarColor; }

    public boolean isNotesPage() { return isNotesPage; }
    public void setNotesPage(boolean isNotesPage) { this.isNotesPage = isNotesPage; }
    public boolean isRewardsPage() { return isRewardsPage; }
    public void setRewardsPage(boolean isRewardsPage) { this.isRewardsPage = isRewardsPage; }
    public boolean isStatPage() { return isStatPage; }
    public void setStatPage(boolean statPage) { this.isStatPage = statPage; }
    public boolean isPerkPage() { return isPerkPage; }
    public void setPerkPage(boolean perkPage) { this.isPerkPage = perkPage; }
    // --- NEW: Challenge Page Getter/Setter ---
    public boolean isChallengePage() { return isChallengePage; }
    public void setChallengePage(boolean challengePage) { this.isChallengePage = challengePage; }

    public int getResetIntervalHours() { return resetIntervalHours; }
    public void setResetIntervalHours(int resetIntervalHours) { this.resetIntervalHours = resetIntervalHours; }
    public boolean isAutoArchive() { return autoArchive; }
    public void setAutoArchive(boolean autoArchive) { this.autoArchive = autoArchive; }
    public boolean isAllowManualArchiving() { return allowManualArchiving; }
    public void setAllowManualArchiving(boolean allowManualArchiving) { this.allowManualArchiving = allowManualArchiving; }
    public boolean isHasStreak() { return hasStreak; }
    public void setHasStreak(boolean hasStreak) { this.hasStreak = hasStreak; }

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
    public boolean isShowTaskType() { return showWorkType; }
    public void setShowTaskType(boolean showWorkType) { this.showWorkType = showWorkType; }
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
    public boolean isEnableLinkCards() { return enableLinkCards; }
    public void setEnableLinkCards(boolean enableLinkCards) { this.enableLinkCards = enableLinkCards; }
    public boolean isEnableIcons() { return enableIcons; }
    public void setEnableIcons(boolean enableIcons) { this.enableIcons = enableIcons; }
    public boolean isEnableZenMode() { return enableZenMode; }
    public void setEnableZenMode(boolean enableZenMode) { this.enableZenMode = enableZenMode; }
    public boolean isEnableOptionalTasks() { return enableOptionalTasks; }
    public void setEnableOptionalTasks(boolean enableOptionalTasks) { this.enableOptionalTasks = enableOptionalTasks; }
    public boolean isEnableTaskStyling() { return enableTaskStyling; }
    public void setEnableTaskStyling(boolean enableTaskStyling) { this.enableTaskStyling = enableTaskStyling; }
    public boolean isShowAnalytics() { return showAnalytics; }
    public void setShowAnalytics(boolean showAnalytics) { this.showAnalytics = showAnalytics; }

    public boolean isEnableTimedTasks() { return enableTimedTasks; }
    public void setEnableTimedTasks(boolean enableTimedTasks) { this.enableTimedTasks = enableTimedTasks; }

    public List<DailyTemplate> getAutoAddTemplates() {
        if (autoAddTemplates == null) autoAddTemplates = new ArrayList<>();
        return autoAddTemplates;
    }
}