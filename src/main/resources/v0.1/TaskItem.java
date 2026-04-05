package com.raeden.ors_to_do;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class TaskItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Priority { LOW, MED, HIGH }
    public enum OriginModule { QUICK, DAILY, WORK }

    private String id;
    private String textContent;
    private Priority priority;
    private boolean isFinished;
    private LocalDateTime dateCreated;
    private LocalDateTime dateCompleted;
    private OriginModule originModule;
    private String colorHex;
    private String prefix;
    private LocalDateTime startDate;
    private LocalDateTime deadline;

    private boolean isArchived = false;
    private boolean isFavorite = false;
    private String workType = ""; // e.g., "Plugin Dev", "Studio Asset", "General"

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public TaskItem(String textContent, Priority priority, OriginModule originModule) {
        this.id = UUID.randomUUID().toString();
        this.textContent = textContent;
        this.priority = priority;
        this.originModule = originModule;
        this.isFinished = false;
        this.dateCreated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public boolean isFinished() { return isFinished; }

    public void setFinished(boolean finished) {
        this.isFinished = finished;
        this.dateCompleted = finished ? LocalDateTime.now() : null;
    }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public LocalDateTime getDateCompleted() { return dateCompleted; }
    public OriginModule getOriginModule() { return originModule; }
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