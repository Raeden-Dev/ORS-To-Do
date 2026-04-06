package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.util.UUID;

public class CustomStat implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- Existing Fields ---
    private String id;
    private String name;
    private String iconSymbol;
    private String backgroundColor;
    private String textColor;

    // --- PHASE 1: NEW RPG FIELDS ---
    private int currentAmount = 0;
    private String description = "";
    private int maxCap = 9999;
    private int atrophyDays = 0; // 0 = no atrophy
    private int lifetimeEarned = 0;
    private int lifetimeLost = 0;
    private int maxLevelReached = 0;

    public CustomStat() {
        this.id = UUID.randomUUID().toString();
    }

    public CustomStat(String name, String iconSymbol, String backgroundColor, String textColor) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.iconSymbol = iconSymbol;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    // --- Existing Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconSymbol() { return iconSymbol; }
    public void setIconSymbol(String iconSymbol) { this.iconSymbol = iconSymbol; }

    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    // --- PHASE 1: NEW RPG Getters & Setters ---
    public int getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(int currentAmount) { this.currentAmount = currentAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMaxCap() { return maxCap; }
    public void setMaxCap(int maxCap) { this.maxCap = maxCap; }

    public int getAtrophyDays() { return atrophyDays; }
    public void setAtrophyDays(int atrophyDays) { this.atrophyDays = atrophyDays; }

    public int getLifetimeEarned() { return lifetimeEarned; }
    public void setLifetimeEarned(int lifetimeEarned) { this.lifetimeEarned = lifetimeEarned; }

    public int getLifetimeLost() { return lifetimeLost; }
    public void setLifetimeLost(int lifetimeLost) { this.lifetimeLost = lifetimeLost; }

    public int getMaxLevelReached() { return maxLevelReached; }
    public void setMaxLevelReached(int maxLevelReached) { this.maxLevelReached = maxLevelReached; }
}