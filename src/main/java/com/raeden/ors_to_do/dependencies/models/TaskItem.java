package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private String workType = "";
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

    private List<String> links = new ArrayList<>();
    private List<TaskLink> taskLinks = new ArrayList<>();

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
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
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
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { this.isArchived = archived; }
}