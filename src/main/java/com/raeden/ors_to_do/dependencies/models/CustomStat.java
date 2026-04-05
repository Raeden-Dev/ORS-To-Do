package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.util.UUID;

public class CustomStat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String iconSymbol;
    private String backgroundColor;
    private String textColor;

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

    // --- Getters & Setters ---
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
}