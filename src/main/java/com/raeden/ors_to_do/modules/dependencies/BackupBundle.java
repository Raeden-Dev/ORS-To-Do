package com.raeden.ors_to_do.dependencies;

import java.io.Serializable;
import java.util.List;

public class BackupBundle implements Serializable {
    private static final long serialVersionUID = 1L;
    private AppStats appStats;
    private List<TaskItem> taskDatabase;

    public BackupBundle(AppStats appStats, List<TaskItem> taskDatabase) {
        this.appStats = appStats;
        this.taskDatabase = taskDatabase;
    }

    public AppStats getAppStats() { return appStats; }
    public List<TaskItem> getTaskDatabase() { return taskDatabase; }
}