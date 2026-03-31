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

    public enum OriginModule { QUICK, DAILY, WORK } // Kept purely to load legacy data

    private String id;
    private String textContent;
    private CustomPriority priority;
    private boolean isFinished;
    private LocalDateTime dateCreated;
    private LocalDateTime dateCompleted;
    private String colorHex;
    private String prefix;
    private LocalDateTime startDate;
    private LocalDateTime deadline;

    private boolean isArchived = false;
    private boolean isFavorite = false;
    private String workType = "";
    private int timeSpentSeconds = 0;

    private List<SubTask> subTasks = new ArrayList<>();
    private boolean isExpanded = false;
    private String prefixColor;

    // Legacy field. DO NOT DELETE.
    private OriginModule originModule;

    // --- NEW: Dynamic Modular ID ---
    private String sectionId;

    // Overloaded Constructor for Old Files (keeps code compiling during Phase 1)
    public TaskItem(String textContent, CustomPriority priority, OriginModule legacyModule) {
        this.id = UUID.randomUUID().toString();
        this.textContent = textContent;
        this.priority = priority;
        this.originModule = legacyModule;
        this.sectionId = legacyModule.name(); // Auto-migrate locally
        this.isFinished = false;
        this.dateCreated = LocalDateTime.now();
    }

    // New Dynamic Constructor
    public TaskItem(String textContent, CustomPriority priority, String sectionId) {
        this.id = UUID.randomUUID().toString();
        this.textContent = textContent;
        this.priority = priority;
        this.sectionId = sectionId;
        this.isFinished = false;
        this.dateCreated = LocalDateTime.now();
    }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public OriginModule getLegacyOriginModule() { return originModule; }

    // THE BRIDGE: Prevents UI crashes during Phase 1 by mocking the old Enum
    public OriginModule getOriginModule() {
        if (originModule != null) return originModule;
        try {
            return OriginModule.valueOf(sectionId);
        } catch (Exception e) {
            return null; // Will safely handle totally custom sections later
        }
    }

    public String getPrefixColor() { return prefixColor; }
    public void setPrefixColor(String prefixColor) { this.prefixColor = prefixColor; }
    public List<SubTask> getSubTasks() {
        if (subTasks == null) subTasks = new ArrayList<>();
        return subTasks;
    }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { this.isExpanded = expanded; }
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