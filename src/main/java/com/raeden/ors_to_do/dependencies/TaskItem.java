package com.raeden.ors_to_do.dependencies; // Adjust this if your actual package is com.raeden.ors_to_do

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class TaskItem implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- The New Custom Priority Class ---
    public static class CustomPriority implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String colorHex;

        public CustomPriority(String name, String colorHex) {
            this.name = name;
            this.colorHex = colorHex;
        }

        public String getName() { return name; }
        public String getColorHex() { return colorHex; }

        @Override
        public String toString() { return name; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CustomPriority that = (CustomPriority) obj;
            return name.equals(that.name);
        }
    }

    public enum OriginModule { QUICK, DAILY, WORK }

    // --- Fields ---
    private String id;
    private String textContent;
    private CustomPriority priority; // Fixed: Now uses CustomPriority exclusively
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
    private String workType = "";

    // --- Constructor ---
    public TaskItem(String textContent, CustomPriority priority, OriginModule originModule) { // Fixed parameter
        this.id = UUID.randomUUID().toString();
        this.textContent = textContent;
        this.priority = priority;
        this.originModule = originModule;
        this.isFinished = false;
        this.dateCreated = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getId() { return id; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public CustomPriority getPriority() { return priority; } // Fixed return type
    public void setPriority(CustomPriority priority) { this.priority = priority; } // Fixed parameter type

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