package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.models.SubTask;
import com.raeden.ors_to_do.dependencies.models.TaskLink;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import static com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs.*;

public class TaskContextMenu {

    private static final String[] DARK_PASTELS = {
            "#2C3E50", "#34495E", "#1A252C", "#2D3748", "#2A4365",
            "#2C5282", "#2B6CB0", "#234E52", "#285E61", "#2C7A7B",
            "#22543D", "#276749", "#2F855A", "#744210", "#975A16",
            "#702459", "#97266D", "#44337A", "#553C9A", "#1A202C"
    };

    public static ContextMenu build(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, Consumer<TaskItem> onGoToPage) {
        ContextMenu contextMenu = new ContextMenu();

        if (onGoToPage != null) {
            MenuItem gotoItem = new MenuItem("Go to card page");
            gotoItem.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");
            gotoItem.setOnAction(e -> onGoToPage.accept(task));
            contextMenu.getItems().addAll(gotoItem, new SeparatorMenuItem());
        }

        MenuItem copyItem = new MenuItem("Copy Card Information");
        copyItem.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            if (config.isShowDate()) sb.append("[").append(task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("] ");
            if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty()) sb.append("[").append(task.getWorkType()).append("] ");
            else if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) sb.append(task.getPrefix()).append(" ");

            sb.append(task.getTextContent());

            if (task.getTaskLinks() != null) {
                for (TaskLink linkObj : task.getTaskLinks()) {
                    String dName = linkObj.getName();
                    if (dName == null || dName.trim().isEmpty() || dName.equalsIgnoreCase("Link") || dName.equals(linkObj.getUrl())) {
                        sb.append("\n    🔗 ").append(linkObj.getUrl());
                    } else {
                        sb.append("\n    🔗 ").append(dName).append(" (").append(linkObj.getUrl()).append(")");
                    }
                }
            }

            for (SubTask sub : task.getSubTasks()) {
                sb.append("\n    > ").append(sub.getTextContent());
            }

            ClipboardContent content = new ClipboardContent();
            content.putString(sb.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });
        contextMenu.getItems().addAll(copyItem, new SeparatorMenuItem());

        // --- NEW: Duplicate Task Functionality ---
        MenuItem duplicateItem = new MenuItem("Duplicate Task");
        duplicateItem.setOnAction(e -> {
            // Create base clone
            TaskItem clone = new TaskItem(task.getTextContent() + " (Copy)", task.getPriority(), task.getSectionId());
            clone.setColorHex(task.getColorHex());
            clone.setPrefix(task.getPrefix());
            clone.setPrefixColor(task.getPrefixColor());
            clone.setIconSymbol(task.getIconSymbol());
            clone.setIconColor(task.getIconColor());
            clone.setWorkType(task.getWorkType());
            clone.setRewardPoints(task.getRewardPoints());
            clone.setPenaltyPoints(task.getPenaltyPoints());
            clone.setCostPoints(task.getCostPoints());

            if (task.isCounterMode()) {
                clone.setCounterMode(true);
                clone.setMaxCount(task.getMaxCount());
            }

            // Deep copy Sub-Tasks
            for (SubTask st : task.getSubTasks()) {
                clone.getSubTasks().add(new SubTask(st.getTextContent()));
            }

            // Deep copy Links
            if (task.getTaskLinks() != null) {
                for (TaskLink tl : task.getTaskLinks()) {
                    clone.getTaskLinks().add(new TaskLink(tl.getName(), tl.getUrl()));
                }
            }

            // Insert exactly below the original task
            int idx = globalDatabase.indexOf(task);
            if (idx != -1) {
                globalDatabase.add(idx + 1, clone);
            } else {
                globalDatabase.add(clone);
            }

            StorageManager.saveTasks(globalDatabase);
            onUpdate.run();
        });
        contextMenu.getItems().addAll(duplicateItem, new SeparatorMenuItem());

        if (config.isAllowFavorite()) {
            MenuItem favItem = new MenuItem(task.isFavorite() ? "Remove Favorite" : "Mark as Favorite");
            favItem.setOnAction(e -> {
                task.setFavorite(!task.isFavorite());
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            });
            contextMenu.getItems().addAll(favItem, new SeparatorMenuItem());
        }

        MenuItem editItem = new MenuItem("Edit Task");
        editItem.setOnAction(e -> TaskDialogs.showEditDialog(task, config, appStats, globalDatabase, onUpdate));
        contextMenu.getItems().add(editItem);

        if (config.isEnableLinks()) {
            MenuItem addLinkItem = new MenuItem("Add Link");
            addLinkItem.setOnAction(e -> TaskDialogs.showLinkDialog(task, null, globalDatabase, onUpdate));
            contextMenu.getItems().add(addLinkItem);
        }

        if (config.isEnableSubTasks()) {
            MenuItem addSubItem = new MenuItem("Add Sub-task");
            addSubItem.setOnAction(e -> showAddSubTaskDialog(task, globalDatabase, onUpdate));
            contextMenu.getItems().add(addSubItem);
        }

        Menu colorMenu = new Menu("Set Background Color");
        for (String hex : DARK_PASTELS) {
            MenuItem colorItem = new MenuItem("");
            Rectangle colorIcon = new Rectangle(14, 14, Color.web(hex));
            colorIcon.setStroke(Color.BLACK);
            colorItem.setGraphic(colorIcon);
            colorItem.setOnAction(e -> { task.setColorHex(hex); StorageManager.saveTasks(globalDatabase); onUpdate.run(); });
            colorMenu.getItems().add(colorItem);
        }
        MenuItem resetColor = new MenuItem("Reset Background");
        resetColor.setOnAction(e -> { task.setColorHex(null); StorageManager.saveTasks(globalDatabase); onUpdate.run(); });
        colorMenu.getItems().addAll(new SeparatorMenuItem(), resetColor);
        contextMenu.getItems().add(colorMenu);

        if (config.isAllowArchive() && !task.isCounterMode()) {
            MenuItem archiveItem = new MenuItem("Archive");
            archiveItem.setOnAction(e -> {
                if(!task.isFinished()) task.setFinished(true);
                task.setArchived(true);
                StorageManager.saveTasks(globalDatabase); onUpdate.run();
            });
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(archiveItem);
        }

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> {
            globalDatabase.remove(task);
            StorageManager.saveTasks(globalDatabase);
            onUpdate.run();
        });

        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(deleteItem);

        return contextMenu;
    }
}