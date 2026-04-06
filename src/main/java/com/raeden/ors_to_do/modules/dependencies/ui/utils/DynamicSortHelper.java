package com.raeden.ors_to_do.modules.dependencies.ui.utils;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;

import java.util.List;

public class DynamicSortHelper {

    public static void sortTasks(List<TaskItem> tasks, String sortMode, SectionConfig config, AppStats appStats) {
        if (!sortMode.equals("Custom Order")) {
            tasks.sort((t1, t2) -> {
                if (config.isNotesPage()) {
                    if (t1.isPinned() && !t2.isPinned()) return -1;
                    if (!t1.isPinned() && t2.isPinned()) return 1;
                }
                switch (sortMode) {
                    case "Oldest First": return t1.getDateCreated().compareTo(t2.getDateCreated());
                    case "Alphabetical": return t1.getTextContent().compareToIgnoreCase(t2.getTextContent());
                    case "Priority: Low to High": return Integer.compare(getPriorityWeight(t1.getPriority(), appStats), getPriorityWeight(t2.getPriority(), appStats));
                    case "Priority: High to Low": return Integer.compare(getPriorityWeight(t2.getPriority(), appStats), getPriorityWeight(t1.getPriority(), appStats));
                    case "Most Recent":
                    default: return t2.getDateCreated().compareTo(t1.getDateCreated());
                }
            });
        } else if (config.isNotesPage()) {
            tasks.sort((t1, t2) -> {
                if (t1.isPinned() && !t2.isPinned()) return -1;
                if (!t1.isPinned() && t2.isPinned()) return 1;
                return 0;
            });
        }
    }

    private static int getPriorityWeight(CustomPriority p, AppStats appStats) {
        if (p == null) return 999;
        int idx = appStats.getCustomPriorities().indexOf(p);
        return idx == -1 ? 999 : idx;
    }
}