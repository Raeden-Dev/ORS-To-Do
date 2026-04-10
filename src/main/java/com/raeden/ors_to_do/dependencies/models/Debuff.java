package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Debuff implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private String description = "";
    private String iconSymbol = "☠";
    private String colorHex = "#8B0000";

    private int requiredTaskCompletions = 0;
    private int currentTaskCompletions = 0;
    private int durationHours = 0;
    private LocalDateTime expiryDate = null;

    private boolean allowStacking = false;
    private int maxStacks = 1;
    private int currentStacks = 1;

    // --- NEW: Aura Flag for Threshold Debuffs ---
    private boolean isAura = false;

    private Map<String, Double> statGainMultipliers = new HashMap<>();
    private Map<String, Integer> statCapReductions = new HashMap<>();
    private Map<String, Double> statGainMultiplierStackReductions = new HashMap<>();
    private Map<String, Integer> statCapReductionStackIncreasers = new HashMap<>();

    public Debuff cloneAsActive() {
        Debuff d = new Debuff();
        d.id = this.id;
        d.name = this.name;
        d.description = this.description;
        d.iconSymbol = this.iconSymbol;
        d.colorHex = this.colorHex;
        d.requiredTaskCompletions = this.requiredTaskCompletions;
        d.currentTaskCompletions = 0;
        d.durationHours = this.durationHours;
        if (this.durationHours > 0) d.expiryDate = LocalDateTime.now().plusHours(this.durationHours);

        d.allowStacking = this.allowStacking;
        d.maxStacks = this.maxStacks;
        d.currentStacks = 1;
        d.isAura = false; // Default to false, handler sets true if needed

        d.statGainMultipliers = new HashMap<>(this.statGainMultipliers);
        d.statCapReductions = new HashMap<>(this.statCapReductions);
        d.statGainMultiplierStackReductions = new HashMap<>(this.statGainMultiplierStackReductions);
        d.statCapReductionStackIncreasers = new HashMap<>(this.statCapReductionStackIncreasers);
        return d;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconSymbol() { return iconSymbol; }
    public void setIconSymbol(String iconSymbol) { this.iconSymbol = iconSymbol; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public int getRequiredTaskCompletions() { return requiredTaskCompletions; }
    public void setRequiredTaskCompletions(int requiredTaskCompletions) { this.requiredTaskCompletions = requiredTaskCompletions; }
    public int getCurrentTaskCompletions() { return currentTaskCompletions; }
    public void setCurrentTaskCompletions(int currentTaskCompletions) { this.currentTaskCompletions = currentTaskCompletions; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isAllowStacking() { return allowStacking; }
    public void setAllowStacking(boolean allowStacking) { this.allowStacking = allowStacking; }
    public int getMaxStacks() { return maxStacks; }
    public void setMaxStacks(int maxStacks) { this.maxStacks = maxStacks; }
    public int getCurrentStacks() { return currentStacks; }
    public void setCurrentStacks(int currentStacks) { this.currentStacks = currentStacks; }

    public boolean isAura() { return isAura; }
    public void setAura(boolean aura) { this.isAura = aura; }

    public Map<String, Double> getStatGainMultipliers() {
        if (statGainMultipliers == null) statGainMultipliers = new HashMap<>();
        return statGainMultipliers;
    }
    public void setStatGainMultipliers(Map<String, Double> statGainMultipliers) { this.statGainMultipliers = statGainMultipliers; }

    public Map<String, Integer> getStatCapReductions() {
        if (statCapReductions == null) statCapReductions = new HashMap<>();
        return statCapReductions;
    }
    public void setStatCapReductions(Map<String, Integer> statCapReductions) { this.statCapReductions = statCapReductions; }

    public Map<String, Double> getStatGainMultiplierStackReductions() {
        if (statGainMultiplierStackReductions == null) statGainMultiplierStackReductions = new HashMap<>();
        return statGainMultiplierStackReductions;
    }
    public void setStatGainMultiplierStackReductions(Map<String, Double> statGainMultiplierStackReductions) { this.statGainMultiplierStackReductions = statGainMultiplierStackReductions; }

    public Map<String, Integer> getStatCapReductionStackIncreasers() {
        if (statCapReductionStackIncreasers == null) statCapReductionStackIncreasers = new HashMap<>();
        return statCapReductionStackIncreasers;
    }
    public void setStatCapReductionStackIncreasers(Map<String, Integer> statCapReductionStackIncreasers) { this.statCapReductionStackIncreasers = statCapReductionStackIncreasers; }
}