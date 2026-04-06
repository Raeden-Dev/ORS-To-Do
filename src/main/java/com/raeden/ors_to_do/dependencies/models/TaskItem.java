package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class TaskItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String textContent;
    private CustomPriority priority;
    private boolean isFinished;
    private LocalDateTime dateCreated;
    private LocalDateTime dateCompleted;
    private String colorHex;
    private String prefix;
    private LocalDateTime startDate;
    private LocalDateTime deadline;

    private boolean isArchived = false;
    private boolean isFavorite = false;
    private String taskType = "";
    private int timeSpentSeconds = 0;

    private List<SubTask> subTasks = new ArrayList<>();
    private boolean isExpanded = false;
    private String prefixColor;

    private OriginModule originModule;
    private String sectionId;

    private boolean isCounterMode = false;
    private int currentCount = 0;
    private int maxCount = 0;

    private int rewardPoints = 0;
    private int penaltyPoints = 0;
    private boolean pointsClaimed = false;
    private boolean penaltyApplied = false;

    private boolean statsExpanded = false;

    private List<String> links = new ArrayList<>();
    private List<TaskLink> taskLinks = new ArrayList<>();

    private Map<String, Integer> statRewards = new HashMap<>();
    private Map<String, Integer> statPenalties = new HashMap<>();

    // --- NEW: Optional Task Flag ---
    private boolean isOptional = false;
    public boolean isOptional() { return isOptional; }
    public void setOptional(boolean optional) { isOptional = optional; }

    public boolean isStatsExpanded() { return statsExpanded; }
    public void setStatsExpanded(boolean statsExpanded) { this.statsExpanded = statsExpanded; }

    private boolean notified24h = false;
    private boolean notified12h = false;
    private boolean notified4h = false;
    private boolean notified2h = false;

    public boolean isNotified24h() { return notified24h; }
    public void setNotified24h(boolean v) { this.notified24h = v; }
    public boolean isNotified12h() { return notified12h; }
    public void setNotified12h(boolean v) { this.notified12h = v; }
    public boolean isNotified4h() { return notified4h; }
    public void setNotified4h(boolean v) { this.notified4h = v; }
    public boolean isNotified2h() { return notified2h; }
    public void setNotified2h(boolean v) { this.notified2h = v; }

    // --- PHASE 4: PERK SYSTEM FIELDS ---
    private Map<String, Integer> statRequirements = new HashMap<>();
    private int perkLevel = 0; // 0 to 5
    private int weeksMaintained = 0;
    private String perkDescription = "";

    public Map<String, Integer> getStatRequirements() {
        if (statRequirements == null) statRequirements = new HashMap<>();
        return statRequirements;
    }
    public void setStatRequirements(Map<String, Integer> statRequirements) { this.statRequirements = statRequirements; }

    public int getPerkLevel() { return perkLevel; }
    public void setPerkLevel(int perkLevel) { this.perkLevel = perkLevel; }

    public int getWeeksMaintained() { return weeksMaintained; }
    public void setWeeksMaintained(int weeksMaintained) { this.weeksMaintained = weeksMaintained; }

    public String getPerkDescription() { return perkDescription; }
    public void setPerkDescription(String perkDescription) { this.perkDescription = perkDescription; }

    // --- NEW: Link Card Properties ---
    private boolean isLinkCard;
    private String linkActionPath;

    public boolean isLinkCard() { return isLinkCard; }
    public void setLinkCard(boolean linkCard) { this.isLinkCard = linkCard; }

    public String getLinkActionPath() { return linkActionPath; }
    public void setLinkActionPath(String linkActionPath) { this.linkActionPath = linkActionPath; }

    // --- NEW: NOTES Specific Variables ---
    private boolean isPinned = false;
    private String customOutlineColor = null;
    private String customSideboxColor = null;

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public String getCustomOutlineColor() { return customOutlineColor; }
    public void setCustomOutlineColor(String color) { this.customOutlineColor = color; }

    public String getCustomSideboxColor() { return customSideboxColor; }
    public void setCustomSideboxColor(String color) { this.customSideboxColor = color; }

    private int costPoints = 0;
    public int getCostPoints() { return costPoints; }
    public void setCostPoints(int costPoints) { this.costPoints = costPoints; }

    private String iconSymbol;
    private String iconColor;

    public String getIconSymbol() { return iconSymbol; }
    public void setIconSymbol(String iconSymbol) { this.iconSymbol = iconSymbol; }
    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }

    public Map<String, Integer> getStatRewards() {
        if (statRewards == null) statRewards = new HashMap<>();
        return statRewards;
    }
    public void setStatRewards(Map<String, Integer> statRewards) { this.statRewards = statRewards; }

    public Map<String, Integer> getStatPenalties() {
        if (statPenalties == null) statPenalties = new HashMap<>();
        return statPenalties;
    }
    public void setStatPenalties(Map<String, Integer> statPenalties) { this.statPenalties = statPenalties; }

    public TaskItem(String textContent, CustomPriority priority, OriginModule legacyModule) {
        this.id = UUID.randomUUID().toString();
        this.textContent = textContent;
        this.priority = priority;
        this.originModule = legacyModule;
        this.sectionId = legacyModule.name();
        this.isFinished = false;
        this.dateCreated = LocalDateTime.now();
    }

    public TaskItem(String textContent, CustomPriority priority, String sectionId) {
        this.id = UUID.randomUUID().toString();
        this.textContent = textContent;
        this.priority = priority;
        this.sectionId = sectionId;
        this.isFinished = false;
        this.dateCreated = LocalDateTime.now();
    }

    public List<TaskLink> getTaskLinks() {
        if (taskLinks == null) taskLinks = new ArrayList<>();

        if (links != null && !links.isEmpty()) {
            for (String oldLink : links) {
                taskLinks.add(new TaskLink(oldLink, oldLink));
            }
            links.clear();
        }
        return taskLinks;
    }

    private List<String> dependsOnTaskIds = new ArrayList<>();

    public List<String> getDependsOnTaskIds() {
        if (dependsOnTaskIds == null) dependsOnTaskIds = new ArrayList<>();
        return dependsOnTaskIds;
    }
    public void setDependsOnTaskIds(List<String> dependsOnTaskIds) { this.dependsOnTaskIds = dependsOnTaskIds; }

    // --- NEW: Timed Task Target ---
    private int targetTimeMinutes = 0;

    public int getTargetTimeMinutes() { return targetTimeMinutes; }
    public void setTargetTimeMinutes(int targetTimeMinutes) { this.targetTimeMinutes = targetTimeMinutes; }

    public boolean isCounterMode() { return isCounterMode; }
    public void setCounterMode(boolean counterMode) { isCounterMode = counterMode; }
    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }
    public int getMaxCount() { return maxCount; }
    public void setMaxCount(int maxCount) { this.maxCount = maxCount; }

    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }
    public int getPenaltyPoints() { return penaltyPoints; }
    public void setPenaltyPoints(int penaltyPoints) { this.penaltyPoints = penaltyPoints; }
    public boolean isPointsClaimed() { return pointsClaimed; }
    public void setPointsClaimed(boolean pointsClaimed) { this.pointsClaimed = pointsClaimed; }
    public boolean isPenaltyApplied() { return penaltyApplied; }
    public void setPenaltyApplied(boolean penaltyApplied) { this.penaltyApplied = penaltyApplied; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public OriginModule getLegacyOriginModule() { return originModule; }
    public OriginModule getOriginModule() {
        if (originModule != null) return originModule;
        try { return OriginModule.valueOf(sectionId); } catch (Exception e) { return null; }
    }

    public String getPrefixColor() { return prefixColor; }
    public void setPrefixColor(String prefixColor) { this.prefixColor = prefixColor; }
    public List<SubTask> getSubTasks() {
        if (subTasks == null) subTasks = new ArrayList<>();
        return subTasks;
    }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { this.isExpanded = expanded; }
    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(int timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }
    public void addTimeSpent(int seconds) { this.timeSpentSeconds += seconds; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getId() { return id; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public CustomPriority getPriority() { return priority; }
    public void setPriority(CustomPriority priority) { this.priority = priority; }
    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) {
        this.isFinished = finished;
        this.dateCompleted = finished ? LocalDateTime.now() : null;
    }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public LocalDateTime getDateCompleted() { return dateCompleted; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
        this.notified24h = false;
        this.notified12h = false;
        this.notified4h = false;
        this.notified2h = false;
    }
    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { this.isArchived = archived; }
}