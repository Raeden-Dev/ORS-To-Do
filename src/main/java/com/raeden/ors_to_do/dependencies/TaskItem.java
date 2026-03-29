package com.raeden.ors_to_do.dependencies;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskItem implements Serializable {
    private static final long serialVersionUID = 1L;

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

    // --- NEW: SubTask Data Model ---
    public static class SubTask implements Serializable {
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
        public void setFinished(boolean finished) { isFinished = finished; }
    }

    public enum OriginModule { QUICK, DAILY, WORK }

    private String id;
    private String textContent;
    private CustomPriority priority;
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
    private int timeSpentSeconds = 0;

    // --- NEW: Sub-Task Variables ---
    private List<SubTask> subTasks = new ArrayList<>();
    private boolean isExpanded = false;

    public TaskItem(String textContent, CustomPriority priority, OriginModule originModule) {
        this.id = UUID.randomUUID().toString();
        this.textContent = textContent;
        this.priority = priority;
        this.originModule = originModule;
        this.isFinished = false;
        this.dateCreated = LocalDateTime.now();
    }

    // --- NEW: Sub-Task Getters/Setters ---
    public List<SubTask> getSubTasks() {
        if (subTasks == null) subTasks = new ArrayList<>(); // Failsafe for old save files
        return subTasks;
    }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void addTimeSpent(int seconds) { this.timeSpentSeconds += seconds; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getId() { return id; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public CustomPriority getPriority() { return priority; }
    public void setPriority(CustomPriority priority) { this.priority = priority; }

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