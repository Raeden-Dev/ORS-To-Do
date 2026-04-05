package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DailyTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    private String prefix;
    private String text;
    private String prefixColor;
    private String bgColor;
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
}