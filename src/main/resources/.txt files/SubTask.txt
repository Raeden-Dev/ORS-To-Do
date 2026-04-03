package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;

public class SubTask implements Serializable {
    private static final long serialVersionUID = 1L;
    private String textContent;
    private boolean isFinished;

    public SubTask(String textContent) {
        this.textContent = textContent;
        this.isFinished = false;
    }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { this.isFinished = finished; }
}