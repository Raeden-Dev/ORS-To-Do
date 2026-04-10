package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SectionConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String sidebarColor = "#FFFFFF";

    private boolean isSeparator = false;

    private int resetIntervalHours = 0;
    private boolean hasStreak = false;
    private int currentStreak = 0;
    private boolean autoArchive = false;
    private boolean allowManualArchiving = true;
    private boolean enableSubTasks = true;
    private boolean showDate = true;
    private boolean showPrefix = false;
    private boolean showTags = false;
    private boolean enableScore = false;
    private boolean enableLinks = false;
    private boolean isRewardsPage = false;

    private boolean isStatPage = false;
    private boolean isPerkPage = false;
    private boolean isChallengePage = false;

    private boolean showPriority = true;
    private boolean trackTime = false;
    private boolean showTaskType = false;
    private boolean allowFavorite = true;
    private boolean showAnalytics = true;
    private boolean enableIcons = false;
    private boolean enableZenMode = true;
    private boolean enableStatsSystem = false;
    private boolean enableLinkCards = false;

    private boolean isNotesPage = false;

    private boolean enableOptionalTasks = false;
    private boolean enableTaskStyling = false;
    private boolean enableTimedTasks = false;

    private boolean allowRepeatingTasks = false;

    // --- NEW: Lock Task After Completion Flag ---
    private boolean lockCompletedTasks = false;

    private List<DailyTemplate> autoAddTemplates = new ArrayList<>();

    public SectionConfig(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSidebarColor() { return sidebarColor; }
    public void setSidebarColor(String sidebarColor) { this.sidebarColor = sidebarColor; }

    public boolean isSeparator() { return isSeparator; }
    public void setSeparator(boolean separator) { this.isSeparator = separator; }

    public int getResetIntervalHours() { return resetIntervalHours; }
    public void setResetIntervalHours(int resetIntervalHours) { this.resetIntervalHours = resetIntervalHours; }

    public boolean isHasStreak() { return hasStreak; }
    public void setHasStreak(boolean hasStreak) { this.hasStreak = hasStreak; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public boolean isAutoArchive() { return autoArchive; }
    public void setAutoArchive(boolean autoArchive) { this.autoArchive = autoArchive; }

    public boolean isAllowManualArchiving() { return allowManualArchiving; }
    public void setAllowManualArchiving(boolean allowManualArchiving) { this.allowManualArchiving = allowManualArchiving; }

    public boolean isEnableSubTasks() { return enableSubTasks; }
    public void setEnableSubTasks(boolean enableSubTasks) { this.enableSubTasks = enableSubTasks; }

    public boolean isShowDate() { return showDate; }
    public void setShowDate(boolean showDate) { this.showDate = showDate; }

    public boolean isShowPrefix() { return showPrefix; }
    public void setShowPrefix(boolean showPrefix) { this.showPrefix = showPrefix; }

    public boolean isShowTags() { return showTags; }
    public void setShowTags(boolean showTags) { this.showTags = showTags; }

    public boolean isEnableScore() { return enableScore; }
    public void setEnableScore(boolean enableScore) { this.enableScore = enableScore; }

    public boolean isEnableLinks() { return enableLinks; }
    public void setEnableLinks(boolean enableLinks) { this.enableLinks = enableLinks; }

    public boolean isRewardsPage() { return isRewardsPage; }
    public void setRewardsPage(boolean rewardsPage) { this.isRewardsPage = rewardsPage; }

    public boolean isStatPage() { return isStatPage; }
    public void setStatPage(boolean statPage) { this.isStatPage = statPage; }

    public boolean isPerkPage() { return isPerkPage; }
    public void setPerkPage(boolean perkPage) { this.isPerkPage = perkPage; }

    public boolean isChallengePage() { return isChallengePage; }
    public void setChallengePage(boolean challengePage) { this.isChallengePage = challengePage; }

    public boolean isShowPriority() { return showPriority; }
    public void setShowPriority(boolean showPriority) { this.showPriority = showPriority; }

    public boolean isTrackTime() { return trackTime; }
    public void setTrackTime(boolean trackTime) { this.trackTime = trackTime; }

    public boolean isShowTaskType() { return showTaskType; }
    public void setShowTaskType(boolean showTaskType) { this.showTaskType = showTaskType; }

    public boolean isAllowFavorite() { return allowFavorite; }
    public void setAllowFavorite(boolean allowFavorite) { this.allowFavorite = allowFavorite; }

    public boolean isShowAnalytics() { return showAnalytics; }
    public void setShowAnalytics(boolean showAnalytics) { this.showAnalytics = showAnalytics; }

    public boolean isEnableIcons() { return enableIcons; }
    public void setEnableIcons(boolean enableIcons) { this.enableIcons = enableIcons; }

    public boolean isEnableZenMode() { return enableZenMode; }
    public void setEnableZenMode(boolean enableZenMode) { this.enableZenMode = enableZenMode; }

    public boolean isEnableStatsSystem() { return enableStatsSystem; }
    public void setEnableStatsSystem(boolean enableStatsSystem) { this.enableStatsSystem = enableStatsSystem; }

    public boolean isEnableLinkCards() { return enableLinkCards; }
    public void setEnableLinkCards(boolean enableLinkCards) { this.enableLinkCards = enableLinkCards; }

    public boolean isNotesPage() { return isNotesPage; }
    public void setNotesPage(boolean notesPage) { this.isNotesPage = notesPage; }

    public boolean isEnableOptionalTasks() { return enableOptionalTasks; }
    public void setEnableOptionalTasks(boolean enableOptionalTasks) { this.enableOptionalTasks = enableOptionalTasks; }

    public boolean isEnableTaskStyling() { return enableTaskStyling; }
    public void setEnableTaskStyling(boolean enableTaskStyling) { this.enableTaskStyling = enableTaskStyling; }

    public boolean isEnableTimedTasks() { return enableTimedTasks; }
    public void setEnableTimedTasks(boolean enableTimedTasks) { this.enableTimedTasks = enableTimedTasks; }

    public boolean isAllowRepeatingTasks() { return allowRepeatingTasks; }
    public void setAllowRepeatingTasks(boolean allowRepeatingTasks) { this.allowRepeatingTasks = allowRepeatingTasks; }

    // --- NEW: Getter & Setter for Lock Completed Tasks ---
    public boolean isLockCompletedTasks() { return lockCompletedTasks; }
    public void setLockCompletedTasks(boolean lockCompletedTasks) { this.lockCompletedTasks = lockCompletedTasks; }

    public List<DailyTemplate> getAutoAddTemplates() {
        if (autoAddTemplates == null) {
            autoAddTemplates = new ArrayList<>();
        }
        return autoAddTemplates;
    }

    public void setAutoAddTemplates(List<DailyTemplate> autoAddTemplates) {
        this.autoAddTemplates = autoAddTemplates;
    }

    @Override
    public String toString() { return name; }
}