package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    private String prefix;
    private String text;
    private String prefixColor;

    // --- Visual Styling ---
    private String bgColor;
    private String customOutlineColor;
    private String customSideboxColor;

    private List<DayOfWeek> activeDays;

    private String iconSymbol;
    private String iconColor;
    public String getIconSymbol() { return iconSymbol; }
    public void setIconSymbol(String iconSymbol) { this.iconSymbol = iconSymbol; }
    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }

    private String priorityName;
    private int rewardPoints;
    private int penaltyPoints;
    private String taskType;
    private List<String> subTaskLines;

    private boolean isOptional = false;
    public boolean isOptional() { return isOptional; }
    public void setOptional(boolean optional) { isOptional = optional; }

    // --- NEW: Repeating Task Properties ---
    private boolean isRepeatingMode = false;
    private int repetitionCount = 0;

    // --- RPG Stat Maps ---
    private Map<String, Integer> statRewards = new HashMap<>();
    private Map<String, Integer> statCapRewards = new HashMap<>();
    private Map<String, Integer> statCosts = new HashMap<>();
    private Map<String, Integer> statPenalties = new HashMap<>();
    private Map<String, Integer> statRequirements = new HashMap<>();

    public DailyTemplate(String prefix, String text, String prefixColor, String bgColor) {
        this.prefix = prefix;
        this.text = text;
        this.prefixColor = prefixColor;
        this.bgColor = bgColor;
        this.activeDays = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
        this.subTaskLines = new ArrayList<>();
    }

    public String getPriorityName() { return priorityName; }
    public void setPriorityName(String priorityName) { this.priorityName = priorityName; }
    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }
    public int getPenaltyPoints() { return penaltyPoints; }
    public void setPenaltyPoints(int penaltyPoints) { this.penaltyPoints = penaltyPoints; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public List<String> getSubTaskLines() {
        if (subTaskLines == null) subTaskLines = new ArrayList<>();
        return subTaskLines;
    }
    public void setSubTaskLines(List<String> subTaskLines) { this.subTaskLines = subTaskLines; }

    public List<DayOfWeek> getActiveDays() {
        if (activeDays == null) activeDays = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
        return activeDays;
    }
    public void setActiveDays(List<DayOfWeek> activeDays) { this.activeDays = activeDays; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getPrefixColor() { return prefixColor; }
    public void setPrefixColor(String prefixColor) { this.prefixColor = prefixColor; }

    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    public String getCustomOutlineColor() { return customOutlineColor; }
    public void setCustomOutlineColor(String customOutlineColor) { this.customOutlineColor = customOutlineColor; }
    public String getCustomSideboxColor() { return customSideboxColor; }
    public void setCustomSideboxColor(String customSideboxColor) { this.customSideboxColor = customSideboxColor; }

    public boolean isRepeatingMode() { return isRepeatingMode; }
    public void setRepeatingMode(boolean repeatingMode) { this.isRepeatingMode = repeatingMode; }
    public int getRepetitionCount() { return repetitionCount; }
    public void setRepetitionCount(int repetitionCount) { this.repetitionCount = repetitionCount; }

    public Map<String, Integer> getStatRewards() {
        if (statRewards == null) statRewards = new HashMap<>();
        return statRewards;
    }
    public void setStatRewards(Map<String, Integer> statRewards) { this.statRewards = statRewards; }

    public Map<String, Integer> getStatCapRewards() {
        if (statCapRewards == null) statCapRewards = new HashMap<>();
        return statCapRewards;
    }
    public void setStatCapRewards(Map<String, Integer> statCapRewards) { this.statCapRewards = statCapRewards; }

    public Map<String, Integer> getStatCosts() {
        if (statCosts == null) statCosts = new HashMap<>();
        return statCosts;
    }
    public void setStatCosts(Map<String, Integer> statCosts) { this.statCosts = statCosts; }

    public Map<String, Integer> getStatPenalties() {
        if (statPenalties == null) statPenalties = new HashMap<>();
        return statPenalties;
    }
    public void setStatPenalties(Map<String, Integer> statPenalties) { this.statPenalties = statPenalties; }

    public Map<String, Integer> getStatRequirements() {
        if (statRequirements == null) statRequirements = new HashMap<>();
        return statRequirements;
    }
    public void setStatRequirements(Map<String, Integer> statRequirements) { this.statRequirements = statRequirements; }
}