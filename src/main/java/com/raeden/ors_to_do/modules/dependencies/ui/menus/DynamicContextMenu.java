package com.raeden.ors_to_do.modules.dependencies.ui.menus;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.List;

public class DynamicContextMenu {

    public static ContextMenu build(SectionConfig config, AppStats appStats, List<TaskItem> db, Runnable refreshListAction, Runnable syncCallback) {
        ContextMenu bgMenu = new ContextMenu();
        bgMenu.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #555555;");

        MenuItem createItem = new MenuItem(config.isNotesPage() ? "Create New Note" : "Create New Task");
        createItem.setStyle("-fx-text-fill: white;");
        createItem.setOnAction(e -> createAndEditTask(false, false, config, appStats, db, refreshListAction, syncCallback));
        bgMenu.getItems().add(createItem);

        if (config.isEnableLinkCards()) {
            MenuItem createLinkItem = new MenuItem("Create Link Card");
            createLinkItem.setStyle("-fx-text-fill: white;");
            createLinkItem.setOnAction(e -> createAndEditTask(true, false, config, appStats, db, refreshListAction, syncCallback));
            bgMenu.getItems().add(createLinkItem);
        }

        if (config.isEnableOptionalTasks()) {
            MenuItem createOptItem = new MenuItem("Create Optional Card");
            createOptItem.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
            createOptItem.setOnAction(e -> createAndEditTask(false, true, config, appStats, db, refreshListAction, syncCallback));
            bgMenu.getItems().add(createOptItem);
        }

        if (appStats.isEnableTextToTask() && !config.isNotesPage()) {
            bgMenu.getItems().add(new SeparatorMenuItem());
            MenuItem batchItem = new MenuItem("Batch to Task");
            batchItem.setStyle("-fx-text-fill: white;");
            batchItem.setOnAction(e -> {
                TaskItem dummy = new TaskItem("", null, config.getId());
                TaskDialogs.showTextToTaskDialog(dummy, db, () -> {
                    refreshListAction.run();
                    if(syncCallback != null) syncCallback.run();
                });
            });
            bgMenu.getItems().add(batchItem);
        }
        return bgMenu;
    }

    private static void createAndEditTask(boolean isLink, boolean isOptional, SectionConfig config, AppStats appStats, List<TaskItem> db, Runnable refresh, Runnable sync) {
        CustomPriority defaultPrio = null;
        if (config.isShowPriority() && !config.isNotesPage() && !appStats.getCustomPriorities().isEmpty() && !isOptional) {
            defaultPrio = appStats.getCustomPriorities().get(0);
        }

        TaskItem newTask = new TaskItem("", defaultPrio, config.getId());
        newTask.setLinkCard(isLink);
        newTask.setOptional(isOptional);
        if (config.isShowTaskType() && !config.isNotesPage()) newTask.setTaskType("General");

        TaskDialogs.showEditDialog(newTask, config, appStats, db, () -> {
            if (newTask.getTextContent() != null && !newTask.getTextContent().trim().isEmpty()) {
                if (!db.contains(newTask)) db.add(newTask);
                StorageManager.saveTasks(db);
            }
            refresh.run();
            if (sync != null) sync.run();
        });
    }
}