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

    private Map<String, Double> statGainMultipliers = new HashMap<>();
    private Map<String, Integer> statCapReductions = new HashMap<>();

    public Debuff cloneAsActive() {
        Debuff d = new Debuff();
        d.name = this.name;
        d.description = this.description;
        d.iconSymbol = this.iconSymbol;
        d.colorHex = this.colorHex;
        d.requiredTaskCompletions = this.requiredTaskCompletions;
        d.currentTaskCompletions = 0;
        d.durationHours = this.durationHours;
        if (this.durationHours > 0) d.expiryDate = LocalDateTime.now().plusHours(this.durationHours);
        d.statGainMultipliers = new HashMap<>(this.statGainMultipliers);
        d.statCapReductions = new HashMap<>(this.statCapReductions);
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
}