package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;
import java.util.UUID;

public class StatThreshold implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private int thresholdValue;
    private boolean isUpperThreshold; // True = Triggers when stat >= value. False = Triggers when stat <= value.
    private String debuffId; // The template ID of the debuff to inflict

    public StatThreshold() {}

    public StatThreshold(int thresholdValue, boolean isUpperThreshold, String debuffId) {
        this.thresholdValue = thresholdValue;
        this.isUpperThreshold = isUpperThreshold;
        this.debuffId = debuffId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(int thresholdValue) { this.thresholdValue = thresholdValue; }

    public boolean isUpperThreshold() { return isUpperThreshold; }
    public void setUpperThreshold(boolean upperThreshold) { this.isUpperThreshold = upperThreshold; }

    public String getDebuffId() { return debuffId; }
    public void setDebuffId(String debuffId) { this.debuffId = debuffId; }
}